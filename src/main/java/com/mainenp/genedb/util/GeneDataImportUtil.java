package com.mainenp.genedb.util;

import com.mainenp.genedb.entity.GeneBasic;
import com.mainenp.genedb.repository.GeneBasicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Gene Data Import Utility
 * Reads custom gene list CSV and HGNC data file, then imports into database
 * Batch insertion with commit every 1,000 records for performance
 */
@Slf4j
@Component
public class GeneDataImportUtil {
    
    private static final int BATCH_SIZE = 1000;
    private static final String CUSTOM_GENE_LIST_PATH = "gene.csv";
    private static final String HGNC_DATA_PATH = "hgnc_complete_set.txt";
    
    @Autowired
    private GeneBasicRepository geneBasicRepository;
    
    /**
     * Execute complete data import process
     * 1. Read custom gene list CSV
     * 2. Read HGNC data file
     * 3. Match and merge data
     * 4. Batch insert into database
     */
    public void importGeneData() {
        try {
            log.info("Starting gene data import process...");
            
            // Step 1: Read custom gene list
            Set<String> customGeneSymbols = readCustomGeneList();
            log.info("Loaded {} custom gene symbols", customGeneSymbols.size());
            
            // Step 2: Read HGNC data
            Map<String, Map<String, String>> hgncData = readHgncData();
            log.info("Loaded {} HGNC gene records", hgncData.size());
            
            // Step 3: Match and merge data
            List<GeneBasic> genesToImport = matchAndMergeData(customGeneSymbols, hgncData);
            log.info("Matched {} genes for import", genesToImport.size());
            
            // Step 4: Batch insert
            batchInsertGenes(genesToImport);
            log.info("Gene data import completed successfully!");
            
        } catch (Exception e) {
            log.error("Error during gene data import: {}", e.getMessage(), e);
            throw new RuntimeException("Gene data import failed", e);
        }
    }
    
    /**
     * Read custom gene list from CSV file
     * Extracts gene symbols from first column
     * @return Set of gene symbols
     */
    private Set<String> readCustomGeneList() throws IOException {
        Set<String> geneSymbols = new HashSet<>();
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CUSTOM_GENE_LIST_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            String line;
            int lineNum = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (lineNum == 1) continue; // Skip header
                
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    String geneSymbol = parts[0].trim().toUpperCase();
                    if (!geneSymbol.isEmpty()) {
                        geneSymbols.add(geneSymbol);
                    }
                }
            }
        }
        
        return geneSymbols;
    }
    
    /**
     * Read HGNC complete set data file
     * Extracts gene information indexed by gene symbol
     * @return Map of gene symbol to gene data
     */
    private Map<String, Map<String, String>> readHgncData() throws IOException {
        Map<String, Map<String, String>> hgncData = new HashMap<>();
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(HGNC_DATA_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            String line;
            String[] headers = null;
            int lineNum = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNum++;
                
                if (lineNum == 1) {
                    headers = line.split("\t");
                    continue;
                }
                
                String[] values = line.split("\t", -1);
                if (values.length < headers.length) {
                    continue;
                }
                
                Map<String, String> geneInfo = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    geneInfo.put(headers[i], i < values.length ? values[i] : "");
                }
                
                String geneSymbol = geneInfo.getOrDefault("symbol", "").toUpperCase();
                if (!geneSymbol.isEmpty()) {
                    hgncData.put(geneSymbol, geneInfo);
                }
            }
        }
        
        return hgncData;
    }
    
    /**
     * Match custom genes with HGNC data and create GeneBasic entities
     * @param customGeneSymbols custom gene symbols
     * @param hgncData HGNC gene data
     * @return List of GeneBasic entities ready for import
     */
    private List<GeneBasic> matchAndMergeData(Set<String> customGeneSymbols, 
                                             Map<String, Map<String, String>> hgncData) {
        List<GeneBasic> genes = new ArrayList<>();
        
        for (String geneSymbol : customGeneSymbols) {
            Map<String, String> hgncInfo = hgncData.get(geneSymbol);
            
            if (hgncInfo != null) {
                GeneBasic gene = GeneBasic.builder()
                        .geneSymbol(geneSymbol)
                        .hgncId(hgncInfo.getOrDefault("hgnc_id", ""))
                        .entrezId(hgncInfo.getOrDefault("entrez_id", ""))
                        .ensemblId(hgncInfo.getOrDefault("ensembl_gene_id", ""))
                        .geneFullName(hgncInfo.getOrDefault("name", ""))
                        .geneType(hgncInfo.getOrDefault("locus_type", ""))
                        .geneFunction(hgncInfo.getOrDefault("function", ""))
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                
                genes.add(gene);
            } else {
                // Create minimal record if HGNC data not found
                GeneBasic gene = GeneBasic.builder()
                        .geneSymbol(geneSymbol)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                
                genes.add(gene);
                log.warn("HGNC data not found for gene: {}", geneSymbol);
            }
        }
        
        return genes;
    }
    
    /**
     * Batch insert genes into database
     * Commits every BATCH_SIZE records to avoid memory issues
     * @param genes list of genes to insert
     */
    private void batchInsertGenes(List<GeneBasic> genes) {
        int total = genes.size();
        int inserted = 0;
        
        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, total);
            List<GeneBasic> batch = genes.subList(i, end);
            
            try {
                geneBasicRepository.saveAll(batch);
                inserted = end;
                log.info("Batch insert progress: {}/{}", inserted, total);
            } catch (Exception e) {
                log.error("Error inserting batch [{}-{}]: {}", i, end, e.getMessage());
                throw new RuntimeException("Batch insert failed", e);
            }
        }
        
        log.info("Successfully inserted {} genes into database", inserted);
    }
}
