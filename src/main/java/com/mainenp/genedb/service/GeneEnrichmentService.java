package com.mainenp.genedb.service;

import com.mainenp.genedb.entity.Gene;
import com.mainenp.genedb.repository.GeneRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@Transactional
public class GeneEnrichmentService {
    
    private final GeneRepository geneRepository;
    private final RestTemplate restTemplate;
    private final UniProtService uniProtService;
    private final NCBIGeneService ncbiGeneService;
    private final RCSBPDBService rcsbpdbService;
    
    public GeneEnrichmentService(GeneRepository geneRepository,
                                 RestTemplate restTemplate,
                                 UniProtService uniProtService,
                                 NCBIGeneService ncbiGeneService,
                                 RCSBPDBService rcsbpdbService) {
        this.geneRepository = geneRepository;
        this.restTemplate = restTemplate;
        this.uniProtService = uniProtService;
        this.ncbiGeneService = ncbiGeneService;
        this.rcsbpdbService = rcsbpdbService;
    }
    
    /**
     * 从 CSV 文件提取基因列表（去掉括号内的 Entrez ID）
     * 格式: GENE_NAME (EntrezID)
     */
    public List<String> extractGeneSymbolsFromCSV(List<String> csvLines) {
        List<String> geneSymbols = new ArrayList<>();
        Pattern pattern = Pattern.compile("^([A-Za-z0-9\\-_]+)\\s*\\(\\d+\\)");
        
        for (String line : csvLines) {
            if (line.trim().isEmpty() || line.startsWith(",")) {
                continue;
            }
            
            String[] parts = line.split(",");
            if (parts.length > 0) {
                String geneNameWithId = parts[0].trim();
                Matcher matcher = pattern.matcher(geneNameWithId);
                
                if (matcher.find()) {
                    geneSymbols.add(matcher.group(1).toUpperCase());
                } else {
                    // 如果格式不匹配，尝试直接提取括号前的部分
                    int parenIndex = geneNameWithId.indexOf('(');
                    if (parenIndex > 0) {
                        geneSymbols.add(geneNameWithId.substring(0, parenIndex).trim().toUpperCase());
                    }
                }
            }
        }
        
        log.info("从 CSV 提取了 {} 个基因符号", geneSymbols.size());
        return geneSymbols;
    }
    
    /**
     * 为基因列表创建基本记录（如果不存在）
     */
    @Async
    public void createGeneRecords(List<String> geneSymbols) {
        log.info("开始为 {} 个基因创建记录...", geneSymbols.size());
        
        List<Gene> genesToSave = new ArrayList<>();
        
        for (String symbol : geneSymbols) {
            if (!geneRepository.existsBySymbol(symbol)) {
                Gene gene = new Gene();
                gene.setSymbol(symbol);
                gene.setName(symbol);
                gene.setDescription("Pending enrichment");
                genesToSave.add(gene);
                
                if (genesToSave.size() >= 1000) {
                    geneRepository.saveAll(genesToSave);
                    log.info("已创建 {} 个基因记录", genesToSave.size());
                    genesToSave.clear();
                }
            }
        }
        
        if (!genesToSave.isEmpty()) {
            geneRepository.saveAll(genesToSave);
            log.info("已创建 {} 个基因记录", genesToSave.size());
        }
    }
    
    /**
     * 从多个公共 API 获取基因信息并更新数据库
     */
    @Async
    public void enrichGenesWithPublicData(List<String> geneSymbols) {
        log.info("开始从公共 API 获取 {} 个基因的信息...", geneSymbols.size());
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        for (String symbol : geneSymbols) {
            try {
                Gene gene = geneRepository.findBySymbol(symbol)
                    .orElseGet(() -> {
                        Gene newGene = new Gene();
                        newGene.setSymbol(symbol);
                        newGene.setName(symbol);
                        return newGene;
                    });
                
                // 1. 从 NCBI Gene 获取基本信息
                enrichFromNCBIGene(gene);
                
                // 2. 从 UniProt 获取蛋白质信息
                if (gene.getUniprotId() != null) {
                    enrichFromUniProt(gene);
                }
                
                // 3. 从 RCSB PDB 获取三维结构
                enrichFromRCSBPDB(gene);
                
                // 4. 从 Ensembl 获取位置信息
                enrichFromEnsembl(gene);
                
                geneRepository.save(gene);
                successCount.incrementAndGet();
                
                if (successCount.get() % 100 == 0) {
                    log.info("已处理 {} 个基因", successCount.get());
                }
                
                // 避免 API 限流，添加延迟
                Thread.sleep(100);
            } catch (Exception e) {
                log.warn("处理基因 {} 失败: {}", symbol, e.getMessage());
                errorCount.incrementAndGet();
            }
        }
        
        log.info("基因信息获取完成。成功: {}, 失败: {}", successCount.get(), errorCount.get());
    }
    
    /**
     * 从 NCBI Gene API 获取基因信息
     */
    private void enrichFromNCBIGene(Gene gene) {
        try {
            // 获取染色体位置信息
            Map<String, Object> location = ncbiGeneService.getChromosomalLocation(gene.getSymbol(), gene.getEntrezId());
            
            if (location != null && !location.isEmpty()) {
                if (location.containsKey("chromosome")) {
                    gene.setChromosome((String) location.get("chromosome"));
                }
                if (location.containsKey("start")) {
                    gene.setStartPosition((Long) location.get("start"));
                }
                if (location.containsKey("end")) {
                    gene.setEndPosition((Long) location.get("end"));
                }
                if (location.containsKey("strand")) {
                    gene.setStrand((String) location.get("strand"));
                }
            }
            
            // 获取基因别名
            List<String> aliases = ncbiGeneService.getGeneAliases(gene.getSymbol(), gene.getEntrezId());
            if (!aliases.isEmpty()) {
                String desc = gene.getDescription() != null ? gene.getDescription() : "";
                gene.setDescription(desc + " (Aliases: " + String.join(", ", aliases) + ")");
            }
        } catch (Exception e) {
            log.warn("从 NCBI 获取基因 {} 信息失败: {}", gene.getSymbol(), e.getMessage());
        }
    }
    
    /**
     * 从 UniProt API 获取蛋白质信息
     */
    private void enrichFromUniProt(Gene gene) {
        try {
            Map<String, Object> proteinInfo = uniProtService.getProteinInfo(gene.getUniprotId());
            
            if (proteinInfo != null) {
                if (proteinInfo.containsKey("biological_process")) {
                    gene.setGoBiologicalProcess((String) proteinInfo.get("biological_process"));
                }
                if (proteinInfo.containsKey("molecular_function")) {
                    gene.setGoMolecularFunction((String) proteinInfo.get("molecular_function"));
                }
                if (proteinInfo.containsKey("cellular_component")) {
                    gene.setGoCellularComponent((String) proteinInfo.get("cellular_component"));
                }
                if (proteinInfo.containsKey("protein_length")) {
                    gene.setProteinLength((Integer) proteinInfo.get("protein_length"));
                }
                if (proteinInfo.containsKey("pdb_id")) {
                    gene.setPdbId((String) proteinInfo.get("pdb_id"));
                }
            }
        } catch (Exception e) {
            log.warn("从 UniProt 获取基因 {} 信息失败: {}", gene.getSymbol(), e.getMessage());
        }
    }
    
    /**
     * 从 RCSB PDB API 获取三维结构信息
     */
    private void enrichFromRCSBPDB(Gene gene) {
        try {
            if (gene.getUniprotId() != null) {
                List<String> pdbIds = rcsbpdbService.getPDBIdsByUniProtId(gene.getUniprotId());
                if (!pdbIds.isEmpty()) {
                    gene.setPdbId(pdbIds.get(0));
                }
            }
        } catch (Exception e) {
            log.warn("从 RCSB PDB 获取基因 {} 信息失败: {}", gene.getSymbol(), e.getMessage());
        }
    }
    
    /**
     * 从 Ensembl API 获取基因位置信息
     */
    private void enrichFromEnsembl(Gene gene) {
        try {
            Map<String, Object> ensemblInfo = getEnsemblGeneInfo(gene.getSymbol());
            
            if (ensemblInfo != null) {
                if (ensemblInfo.containsKey("seq_region_name")) {
                    gene.setChromosome((String) ensemblInfo.get("seq_region_name"));
                }
                if (ensemblInfo.containsKey("start")) {
                    gene.setStartPosition((Long) ensemblInfo.get("start"));
                }
                if (ensemblInfo.containsKey("end")) {
                    gene.setEndPosition((Long) ensemblInfo.get("end"));
                }
                if (ensemblInfo.containsKey("strand")) {
                    Object strandObj = ensemblInfo.get("strand");
                    if (strandObj instanceof Integer) {
                        gene.setStrand(((Integer) strandObj) == 1 ? "+" : "-");
                    }
                }
                if (ensemblInfo.containsKey("biotype")) {
                    gene.setBiotype((String) ensemblInfo.get("biotype"));
                }
            }
        } catch (Exception e) {
            log.warn("从 Ensembl 获取基因 {} 信息失败: {}", gene.getSymbol(), e.getMessage());
        }
    }
    
    /**
     * 调用 Ensembl REST API
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getEnsemblGeneInfo(String geneSymbol) {
        try {
            String url = String.format(
                "https://rest.ensembl.org/lookup/symbol/homo_sapiens/%s?expand=1",
                geneSymbol
            );
            
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.warn("Ensembl API 调用失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取基因富集统计信息
     */
    public Map<String, Object> getEnrichmentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalGenes = geneRepository.count();
        long genesWithDescription = geneRepository.countByDescriptionNotNull();
        long genesWithUniProt = geneRepository.countByUniprotIdNotNull();
        long genesWithPDB = geneRepository.countByPdbIdNotNull();
        long genesWithChromosome = geneRepository.countByChromosomeNotNull();
        
        stats.put("totalGenes", totalGenes);
        stats.put("genesWithDescription", genesWithDescription);
        stats.put("genesWithUniProt", genesWithUniProt);
        stats.put("genesWithPDB", genesWithPDB);
        stats.put("genesWithChromosome", genesWithChromosome);
        stats.put("enrichmentPercentage", totalGenes > 0 ? 
            (genesWithDescription * 100.0 / totalGenes) : 0);
        
        return stats;
    }
}
