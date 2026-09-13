package com.mainenp.genedb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class NCBIGeneService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String NCBI_ESEARCH_API = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi";
    private static final String NCBI_EFETCH_API = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi";
    private static final String NCBI_ELINK_API = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi";
    
    public NCBIGeneService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Get gene aliases from NCBI Gene database
     */
    public List<String> getGeneAliases(String geneSymbol, String entrezId) {
        List<String> aliases = new ArrayList<>();
        try {
            if (entrezId == null || entrezId.isEmpty()) {
                entrezId = searchGeneId(geneSymbol);
            }
            
            if (entrezId == null) {
                return aliases;
            }
            
            String url = NCBI_EFETCH_API + "?db=gene&id=" + entrezId + "&rettype=json&retmode=json";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            JsonNode genes = root.path("result").path("genes");
            if (genes.isArray() && genes.size() > 0) {
                JsonNode gene = genes.get(0);
                JsonNode description = gene.path("description");
                
                // Extract aliases from description
                String descText = description.asText();
                if (descText.contains("Also known as")) {
                    String[] parts = descText.split("Also known as");
                    if (parts.length > 1) {
                        String[] aliasList = parts[1].split(",");
                        for (String alias : aliasList) {
                            String cleaned = alias.trim().replaceAll("[^a-zA-Z0-9-]", "");
                            if (!cleaned.isEmpty()) {
                                aliases.add(cleaned);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching gene aliases for {}: {}", geneSymbol, e.getMessage());
        }
        return aliases;
    }
    
    /**
     * Get chromosomal location from NCBI Gene database
     */
    public Map<String, Object> getChromosomalLocation(String geneSymbol, String entrezId) {
        Map<String, Object> location = new HashMap<>();
        try {
            if (entrezId == null || entrezId.isEmpty()) {
                entrezId = searchGeneId(geneSymbol);
            }
            
            if (entrezId == null) {
                return location;
            }
            
            String url = NCBI_EFETCH_API + "?db=gene&id=" + entrezId + "&rettype=json&retmode=json";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            JsonNode genes = root.path("result").path("genes");
            if (genes.isArray() && genes.size() > 0) {
                JsonNode gene = genes.get(0);
                JsonNode genomicInfo = gene.path("genomic-regions");
                
                if (genomicInfo.isArray() && genomicInfo.size() > 0) {
                    JsonNode region = genomicInfo.get(0);
                    location.put("chromosome", region.path("accession").asText());
                    location.put("strand", region.path("strand").asText());
                    
                    JsonNode intervals = region.path("intervals");
                    if (intervals.isArray() && intervals.size() > 0) {
                        JsonNode interval = intervals.get(0);
                        location.put("start", interval.path("from").asLong());
                        location.put("end", interval.path("to").asLong());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching chromosomal location for {}: {}", geneSymbol, e.getMessage());
        }
        return location;
    }
    
    /**
     * Get related genes (orthologs/paralogs) from NCBI Gene database
     */
    public List<Map<String, Object>> getRelatedGenes(String entrezId) {
        List<Map<String, Object>> relatedGenes = new ArrayList<>();
        try {
            String url = NCBI_ELINK_API + "?dbfrom=gene&db=gene&id=" + entrezId + "&rettype=json&retmode=json";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            JsonNode linksets = root.path("linksets");
            if (linksets.isArray() && linksets.size() > 0) {
                JsonNode linkset = linksets.get(0);
                JsonNode links = linkset.path("linksetdbs");
                
                if (links.isArray()) {
                    for (JsonNode link : links) {
                        JsonNode ids = link.path("links");
                        if (ids.isArray()) {
                            for (JsonNode id : ids) {
                                Map<String, Object> gene = new HashMap<>();
                                gene.put("entrezId", id.asText());
                                gene.put("type", link.path("linkname").asText());
                                relatedGenes.add(gene);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching related genes for {}: {}", entrezId, e.getMessage());
        }
        return relatedGenes;
    }
    
    /**
     * Search for gene ID by symbol
     */
    private String searchGeneId(String geneSymbol) {
        try {
            String url = NCBI_ESEARCH_API + "?db=gene&term=" + geneSymbol + "[GENE]&rettype=json&retmode=json";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            JsonNode idList = root.path("esearchresult").path("idlist");
            if (idList.isArray() && idList.size() > 0) {
                return idList.get(0).asText();
            }
        } catch (Exception e) {
            log.error("Error searching gene ID for {}: {}", geneSymbol, e.getMessage());
        }
        return null;
    }
}
