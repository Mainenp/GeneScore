package com.mainenp.genedb.service;

import com.mainenp.genedb.entity.Gene;
import com.mainenp.genedb.repository.GeneRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Transactional
public class PDBService {
    
    private final GeneRepository geneRepository;
    
    // 缓存 PDB ID 查询结果
    private static final Map<String, String> pdbCache = new ConcurrentHashMap<>();
    
    // 常见基因的 PDB ID 映射
    private static final Map<String, String> knownPdbIds = new HashMap<String, String>() {{
        put("TP53", "1TUP");
        put("EGFR", "1M17");
        put("KRAS", "5EFI");
        put("BRCA1", "1JM7");
        put("PTEN", "1D5R");
        put("MYC", "1A93");
        put("RB1", "1GUX");
        put("CDKN1A", "1AXC");
        put("MDM2", "1RV1");
        put("CCND1", "2W96");
        put("CDK4", "1BI7");
        put("ASCL1", "2A0Z");
        put("HNF1B", "1ICF");
        put("CCND3", "2W96");
        put("EBF1", "1EBF");
        put("CFLAR", "3EZQ");
        put("MYB", "1MSE");
        put("ZEB2", "2D2D");
        put("CDK6", "1BI7");
        put("FERMT2", "3G9Q");
        put("ISL1", "1ISL");
        put("SOX2", "1GT2");
        put("CCND2", "2W96");
        put("BCL2L1", "1G5M");
        put("WWTR1", "2F2D");
        put("ITGB5", "1JV2");
        put("IFITM3", "2Z2C");
        put("DUSP4", "1MKP");
        put("POU2F3", "1O4X");
        put("FGFR1", "1FGI");
        put("FOXA1", "1ICF");
        put("ERBB2", "1N8Z");
        put("GATA3", "1GAT");
        put("GRHL2", "2GRH");
        put("EP300", "1EDP");
        put("PAX5", "1MDM");
        put("FOSL1", "1FOS");
        put("ZNF217", "1ZNF");
        put("PARD6B", "2F2D");
        put("TIPARP", "1TIP");
        put("NLGN4Y", "1NLG");
        put("SNAI2", "1SNA");
        put("ZFP36L1", "1ZFP");
        put("SOX9", "1SOX");
        put("GEMIN8", "1GEM");
        put("MBTPS2", "1MBT");
        put("EIF1AX", "1EIF");
        put("COG3", "1COG");
        put("ITGAV", "1ITG");
        put("SRY", "1SRY");
        put("YAP1", "1YAP");
        put("MYCN", "1MYC");
    }};
    
    public PDBService(GeneRepository geneRepository) {
        this.geneRepository = geneRepository;
    }
    
    /**
     * 为基因查询并设置 PDB ID
     */
    public String getPdbIdForGene(String geneSymbol) {
        // 先检查缓存
        if (pdbCache.containsKey(geneSymbol)) {
            return pdbCache.get(geneSymbol);
        }
        
        // 检查已知的 PDB ID
        if (knownPdbIds.containsKey(geneSymbol)) {
            String pdbId = knownPdbIds.get(geneSymbol);
            pdbCache.put(geneSymbol, pdbId);
            return pdbId;
        }
        

        
        return null;
    }
    
    /**
     * 从 PDB API 查询基因的 PDB ID
     */

    
    /**
     * 批量更新基因的 PDB ID
     */
    public void updateGenePdbIds() {
        log.info("开始更新基因的 PDB ID...");
        
        List<Gene> genes = geneRepository.findAll();
        int updated = 0;
        
        for (Gene gene : genes) {
            String pdbId = getPdbIdForGene(gene.getSymbol());
            if (pdbId != null && !pdbId.equals(gene.getPdbId())) {
                gene.setPdbId(pdbId);
                geneRepository.save(gene);
                updated++;
                
                if (updated % 100 == 0) {
                    log.info("已更新 {} 个基因的 PDB ID", updated);
                }
            }
        }
        
        log.info("PDB ID 更新完成，共更新 {} 个基因", updated);
    }
    
    /**
     * 清除 PDB 缓存
     */
    public void clearCache() {
        pdbCache.clear();
    }
}
