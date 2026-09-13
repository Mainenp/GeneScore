package com.mainenp.genedb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainenp.genedb.entity.Gene;
import com.mainenp.genedb.entity.GeneBasic;
import com.mainenp.genedb.entity.GeneCache;
import com.mainenp.genedb.repository.GeneBasicRepository;
import com.mainenp.genedb.repository.GeneCacheRepository;
import com.mainenp.genedb.repository.GeneRepository;
import com.mainenp.genedb.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Gene Detail Service
 * Retrieves complete gene information from database and external APIs
 * Uses caching to avoid repeated API calls
 * Implements parallel API calls using CompletableFuture
 */
@Slf4j
@Service
public class GeneDetailService {
    
    @Autowired
    private GeneBasicRepository geneBasicRepository;
    
    @Autowired
    private GeneRepository geneRepository;
    
    @Autowired
    private GeneCacheRepository geneCacheRepository;
    
    @Autowired
    private AlphaFoldApiClient alphaFoldApiClient;
    
    @Autowired
    private GTExApiClient gtexApiClient;
    
    @Autowired
    private StringDbApiClient stringDbApiClient;
    
    @Autowired
    private DGIdbApiClient dgidbApiClient;
    
    @Autowired
    private KEGGApiClient keggApiClient;
    
    @Autowired
    private ReactomeApiClient reactomeApiClient;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 内存中的基因数据库，用于在数据库连接失败时提供基本信息
    private final Map<String, Gene> inMemoryGeneDatabase = new HashMap<>();
    
    public GeneDetailService() {
        // 初始化内存中的基因数据库
        initializeInMemoryDatabase();
    }
    
    private void initializeInMemoryDatabase() {
        // 添加KRAS基因
        Gene kras = new Gene();
        kras.setId(1L);
        kras.setSymbol("KRAS");
        kras.setName("Kirsten rat sarcoma viral oncogene homolog");
        kras.setEntrezId("3845");
        kras.setUniprotId("P01116");
        kras.setPdbId("4LQM");
        kras.setDescription("This gene encodes a GTPase that functions as an oncogene in many cancers. It is one of the most frequently mutated genes in human malignancies.");
        inMemoryGeneDatabase.put("KRAS", kras);
        
        // 添加EGFR基因
        Gene egfr = new Gene();
        egfr.setId(2L);
        egfr.setSymbol("EGFR");
        egfr.setName("Epidermal growth factor receptor");
        egfr.setEntrezId("1956");
        egfr.setUniprotId("P00533");
        egfr.setPdbId("1M17");
        egfr.setDescription("This gene encodes a receptor tyrosine kinase that regulates cell growth and differentiation. It is frequently overexpressed in various cancers.");
        inMemoryGeneDatabase.put("EGFR", egfr);
        
        // 添加TP53基因
        Gene tp53 = new Gene();
        tp53.setId(3L);
        tp53.setSymbol("TP53");
        tp53.setName("Tumor protein p53");
        tp53.setEntrezId("7157");
        tp53.setUniprotId("P04637");
        tp53.setPdbId("1TUP");
        tp53.setDescription("This gene encodes a tumor suppressor protein that regulates cell cycle progression and apoptosis. It is frequently mutated in human cancers.");
        inMemoryGeneDatabase.put("TP53", tp53);
    }
    
    /**
     * Get complete gene details by gene symbol
     * Fetches data from cache first, then calls APIs if needed
     * Uses parallel execution for better performance
     * @param geneSymbol the gene symbol
     * @return Map containing all gene data
     */
    public Map<String, Object> getGeneDetail(String geneSymbol) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Get basic gene information from Gene repository (which has pdbId)
            Optional<Gene> geneOptional = geneRepository.findBySymbol(geneSymbol);
            if (!geneOptional.isPresent()) {
                // Fallback to GeneBasic if Gene not found
                GeneBasic geneBasic = geneBasicRepository.findByGeneSymbol(geneSymbol).orElse(null);
                if (geneBasic == null) {
                    // Fallback to in-memory database if both repositories fail
                    Gene inMemoryGene = inMemoryGeneDatabase.get(geneSymbol);
                    if (inMemoryGene == null) {
                        result.put("error", "Gene not found: " + geneSymbol);
                        return result;
                    }
                    result.put("basic", convertToMap(inMemoryGene));
                } else {
                    result.put("basic", convertToMap(geneBasic));
                }
            } else {
                Gene gene = geneOptional.get();
                result.put("basic", convertToMap(gene));
            }
            
            // Get uniprotId from basic gene info
            final String[] uniprotId = new String[1];
            uniprotId[0] = "";
            if (result.containsKey("basic")) {
                Map<String, Object> basicInfo = (Map<String, Object>) result.get("basic");
                if (basicInfo.containsKey("uniprotId")) {
                    uniprotId[0] = basicInfo.get("uniprotId").toString();
                }
            }
            
            // Step 2: Fetch all data in parallel
            CompletableFuture<String> structure3dFuture = CompletableFuture.supplyAsync(
                    () -> getOrFetchCachedData(geneSymbol, GeneCache.DataType.STRUCTURE_3D, 
                            () -> alphaFoldApiClient.get3DStructure(uniprotId[0]))
            );
            
            // Get entrezId and ensemblId from basic gene info
            final String[] entrezId = new String[1];
            final String[] ensemblId = new String[1];
            entrezId[0] = "";
            ensemblId[0] = "";
            if (result.containsKey("basic")) {
                Map<String, Object> basicInfo = (Map<String, Object>) result.get("basic");
                if (basicInfo.containsKey("entrezId")) {
                    entrezId[0] = basicInfo.get("entrezId").toString();
                }
                if (basicInfo.containsKey("ensemblId")) {
                    ensemblId[0] = basicInfo.get("ensemblId").toString();
                }
            }
            
            CompletableFuture<String> omicsFuture = CompletableFuture.supplyAsync(
                    () -> getOrFetchCachedData(geneSymbol, GeneCache.DataType.OMICS,
                            () -> gtexApiClient.getExpressionData(entrezId[0]))
            );
            
            CompletableFuture<String> networkFuture = CompletableFuture.supplyAsync(
                    () -> getOrFetchCachedData(geneSymbol, GeneCache.DataType.NETWORK,
                            () -> stringDbApiClient.getInteractionNetwork(geneSymbol))
            );
            
            CompletableFuture<String> drugFuture = CompletableFuture.supplyAsync(
                    () -> getOrFetchCachedData(geneSymbol, GeneCache.DataType.DRUG,
                            () -> dgidbApiClient.getDrugInteractions(geneSymbol))
            );
            
            CompletableFuture<String> pathwayFuture = CompletableFuture.supplyAsync(
                    () -> getOrFetchCachedData(geneSymbol, GeneCache.DataType.PATHWAY,
                            () -> combinePathwayData(geneSymbol, ensemblId[0]))
            );
            
            // Wait for all futures to complete
            CompletableFuture.allOf(structure3dFuture, omicsFuture, networkFuture, 
                    drugFuture, pathwayFuture).join();
            
            // Step 3: Aggregate results with safe defaults
            Object structure3dData = parseJson(structure3dFuture.get());
            Object omicsData = parseJson(omicsFuture.get());
            Object networkData = parseJson(networkFuture.get());
            Object drugsData = parseJson(drugFuture.get());
            Object pathwaysData = parseJson(pathwayFuture.get());
            
            // 确保所有数据都是正确的类型
            result.put("structure3d", structure3dData != null ? structure3dData : new HashMap<>());
            result.put("omics", omicsData != null ? (omicsData instanceof List ? omicsData : new ArrayList<>()) : new ArrayList<>());
            result.put("network", networkData != null ? networkData : new HashMap<>());
            result.put("drugs", drugsData != null ? (drugsData instanceof List ? drugsData : new ArrayList<>()) : new ArrayList<>());
            result.put("pathways", pathwaysData != null ? pathwaysData : new HashMap<>());
            
            log.info("Successfully retrieved complete details for gene: {}", geneSymbol);
            
        } catch (Exception e) {
            log.error("Error retrieving gene details for {}: {}", geneSymbol, e.getMessage());
            // Fallback to in-memory database if exception occurs
            Gene inMemoryGene = inMemoryGeneDatabase.get(geneSymbol);
            if (inMemoryGene != null) {
                result.put("basic", convertToMap(inMemoryGene));
                result.put("structure3d", new HashMap<>());
                result.put("omics", new ArrayList<>());
                result.put("network", new HashMap<>());
                result.put("drugs", new ArrayList<>());
                result.put("pathways", new HashMap<>());
                log.info("Using in-memory data for gene: {}", geneSymbol);
            } else {
                result.put("error", "Failed to retrieve gene details: " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * Get basic gene information from database
     * @param geneSymbol the gene symbol
     * @return GeneBasic entity or null if not found
     */
    public GeneBasic getBasicInfo(String geneSymbol) {
        return geneBasicRepository.findByGeneSymbol(geneSymbol).orElse(null);
    }
    
    /**
     * Get or fetch cached data
     * Checks cache first, fetches from API if cache miss or expired
     * @param geneSymbol the gene symbol
     * @param dataType the data type
     * @param apiFetcher function to fetch from API
     * @return JSON string of data
     */
    private String getOrFetchCachedData(String geneSymbol, GeneCache.DataType dataType,
                                       ApiDataFetcher apiFetcher) {
        try {
            // Check cache
            Optional<GeneCache> cached = geneCacheRepository.findValidCache(
                    geneSymbol, dataType, LocalDateTime.now());
            
            if (cached.isPresent()) {
                log.debug("Cache hit for {} - {}", geneSymbol, dataType);
                return cached.get().getDataContent();
            }
            
            // Cache miss - fetch from API
            log.debug("Cache miss for {} - {}, fetching from API", geneSymbol, dataType);
            String data = apiFetcher.fetch();
            
            // Validate data before storing in cache
            if (isValidJson(data)) {
                // Store in cache
                GeneCache cacheEntry = GeneCache.builder()
                        .geneSymbol(geneSymbol)
                        .dataType(dataType)
                        .dataContent(data)
                        .cacheTime(LocalDateTime.now())
                        .expireTime(LocalDateTime.now().plusDays(1))
                        .build();
                
                geneCacheRepository.save(cacheEntry);
                return data;
            } else {
                log.warn("Invalid JSON data received for {} - {}, not storing in cache", geneSymbol, dataType);
                return getDefaultJsonForType(dataType);
            }
            
        } catch (Exception e) {
            log.error("Error getting cached data for {} - {}: {}", geneSymbol, dataType, e.getMessage());
            return getDefaultJsonForType(dataType);
        }
    }
    
    /**
     * Get appropriate default JSON based on data type
     */
    private String getDefaultJsonForType(GeneCache.DataType dataType) {
        switch (dataType) {
            case DRUG:
            case OMICS:
                return "[]";
            case STRUCTURE_3D:
            case NETWORK:
            case PATHWAY:
            default:
                return "{}";
        }
    }
    
    /**
     * Combine pathway data from KEGG and Reactome
     * @param geneSymbol the gene symbol
     * @param ensemblId the Ensembl gene ID
     * @return combined pathway data as JSON string
     */
    private String combinePathwayData(String geneSymbol, String ensemblId) {
        try {
            String keggData = keggApiClient.getPathways(geneSymbol);
            String reactomeData = reactomeApiClient.getPathways(ensemblId);
            
            Map<String, Object> combined = new HashMap<>();
            combined.put("kegg", parseJson(keggData));
            combined.put("reactome", parseJson(reactomeData));
            
            return objectMapper.writeValueAsString(combined);
        } catch (Exception e) {
            log.error("Error combining pathway data: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * Convert GeneBasic entity to Map
     * @param geneBasic the gene basic entity
     * @return Map representation
     */
    private Map<String, Object> convertToMap(GeneBasic geneBasic) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", geneBasic.getId());
        map.put("geneSymbol", geneBasic.getGeneSymbol());
        map.put("hgncId", geneBasic.getHgncId());
        map.put("entrezId", geneBasic.getEntrezId());
        map.put("ensemblId", geneBasic.getEnsemblId());
        map.put("geneFullName", geneBasic.getGeneFullName());
        map.put("geneType", geneBasic.getGeneType());
        map.put("geneFunction", geneBasic.getGeneFunction());
        return map;
    }
    
    /**
     * Convert Gene entity to Map
     * @param gene the gene entity
     * @return Map representation
     */
    private Map<String, Object> convertToMap(Gene gene) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", gene.getId());
        map.put("geneSymbol", gene.getSymbol());
        map.put("name", gene.getName());
        map.put("entrezId", gene.getEntrezId());
        map.put("uniprotId", gene.getUniprotId());
        map.put("description", gene.getDescription());
        map.put("pdbId", gene.getPdbId());
        return map;
    }
    
    /**
     * Parse JSON string to Object
     * @param jsonString JSON string
     * @return parsed object or empty map if parsing fails
     */
    private Object parseJson(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Object.class);
        } catch (Exception e) {
            log.warn("Error parsing JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Validate if string is valid JSON
     * @param jsonString JSON string to validate
     * @return true if valid JSON, false otherwise
     */
    private boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Functional interface for API data fetching
     */
    @FunctionalInterface
    private interface ApiDataFetcher {
        String fetch() throws Exception;
    }
}
