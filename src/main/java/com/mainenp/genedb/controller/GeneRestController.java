package com.mainenp.genedb.controller;

import com.mainenp.genedb.entity.GeneScoreDb;
import com.mainenp.genedb.service.GeneDetailService;
import com.mainenp.genedb.service.GeneScoreDbImportService;
import com.mainenp.genedb.service.GeneSearchService;
import com.mainenp.genedb.service.PpiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gene REST Controller
 * Exposes RESTful APIs for gene search and detail retrieval
 * All responses follow unified format: {code, msg, data}
 */
@Slf4j
@RestController
@RequestMapping("/api/gene")
public class GeneRestController {
    
    @Autowired
    private GeneSearchService geneSearchService;
    
    @Autowired
    private GeneDetailService geneDetailService;

    @Autowired
    private GeneScoreDbImportService geneScoreDbImportService;

    @Autowired
    private PpiService ppiService;
    
    /**
     * Fuzzy search genes by keyword
     * GET /api/gene/search?keyword={keyword}
     * @param keyword search keyword
     * @return unified response with search results
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchGenes(
            @RequestParam(value = "keyword", required = false) String keyword) {
        
        log.info("Received gene search request with keyword: {}", keyword);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("code", 400);
                response.put("msg", "Keyword cannot be empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Map<String, Object>> results = geneSearchService.searchGenes(keyword);
            
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", results);
            
            log.info("Gene search completed, found {} results", results.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during gene search: {}", e.getMessage(), e);
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get complete gene details by gene symbol
     * GET /api/gene/detail/{geneSymbol}
     * Returns: basic info + 3D structure + omics + network + drugs + pathways
     * @param geneSymbol the gene symbol
     * @return unified response with complete gene details
     */
    @GetMapping("/detail/{geneSymbol}")
    public ResponseEntity<Map<String, Object>> getGeneDetail(
            @PathVariable String geneSymbol) {
        
        log.info("Received gene detail request for: {}", geneSymbol);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (geneSymbol == null || geneSymbol.trim().isEmpty()) {
                response.put("code", 400);
                response.put("msg", "Gene symbol cannot be empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> geneDetail = geneDetailService.getGeneDetail(geneSymbol.toUpperCase());
            
            if (geneDetail.containsKey("error")) {
                response.put("code", 404);
                response.put("msg", geneDetail.get("error").toString());
                response.put("data", null);
                return ResponseEntity.status(404).body(response);
            }
            
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", geneDetail);
            
            log.info("Gene detail retrieved successfully for: {}", geneSymbol);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving gene detail for {}: {}", geneSymbol, e.getMessage(), e);
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get total number of genes in database
     * GET /api/gene/count
     * @return unified response with gene count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getGeneCount() {
        
        log.info("Received gene count request");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            long count = geneSearchService.getTotalGeneCount();
            
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", Map.of("totalGenes", count));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting gene count: {}", e.getMessage(), e);
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Check if gene exists
     * GET /api/gene/exists/{geneSymbol}
     * @param geneSymbol the gene symbol
     * @return unified response with existence status
     */
    @GetMapping("/exists/{geneSymbol}")
    public ResponseEntity<Map<String, Object>> checkGeneExists(
            @PathVariable String geneSymbol) {
        
        log.info("Checking if gene exists: {}", geneSymbol);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = geneSearchService.geneExists(geneSymbol.toUpperCase());
            
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", Map.of("exists", exists));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking gene existence for {}: {}", geneSymbol, e.getMessage(), e);
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get gene scores from gene_score_db table
     * GET /api/gene/scores/{geneSymbol}
     * @param geneSymbol the gene symbol
     * @return unified response with gene scores for all tissues
     */
    @GetMapping("/scores/{geneSymbol}")
    public ResponseEntity<Map<String, Object>> getGeneScores(
            @PathVariable String geneSymbol) {
        
        log.info("Received gene scores request for: {}", geneSymbol);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (geneSymbol == null || geneSymbol.trim().isEmpty()) {
                response.put("code", 400);
                response.put("msg", "Gene symbol cannot be empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            GeneScoreDb geneScoreDb = geneScoreDbImportService.getGeneScoreDb(geneSymbol.toUpperCase());
            
            if (geneScoreDb == null) {
                response.put("code", 404);
                response.put("msg", "Gene scores not found");
                response.put("data", null);
                return ResponseEntity.status(404).body(response);
            }
            
            // Convert to map format
            Map<String, Double> scores = new HashMap<>();
            scores.put("Bowel", geneScoreDb.getBowel());
            scores.put("Breast", geneScoreDb.getBreast());
            scores.put("Brain", geneScoreDb.getBrain());
            scores.put("Stomach", geneScoreDb.getStomach());
            scores.put("Head and Neck", geneScoreDb.getHeadAndNeck());
            scores.put("Kidney", geneScoreDb.getKidney());
            scores.put("Liver", geneScoreDb.getLiver());
            scores.put("Lung", geneScoreDb.getLung());
            scores.put("Ovary", geneScoreDb.getOvary());
            scores.put("Pancreas", geneScoreDb.getPancreas());
            scores.put("Prostate", geneScoreDb.getProstate());
            scores.put("Skin", geneScoreDb.getSkin());
            
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", scores);
            
            log.info("Gene scores retrieved successfully for: {}", geneSymbol);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving gene scores for {}: {}", geneSymbol, e.getMessage(), e);
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get PPI network data for a gene
     * GET /api/gene/ppi/{geneSymbol}?depth=1&maxNodes=50
     * @param geneSymbol the gene symbol
     * @param depth max depth (default 1)
     * @param maxNodes max nodes (default 50)
     * @return unified response with PPI network data
     */
    @GetMapping("/ppi/{geneSymbol}")
    public ResponseEntity<Map<String, Object>> getPpiNetwork(
            @PathVariable String geneSymbol,
            @RequestParam(value = "depth", defaultValue = "1") int depth,
            @RequestParam(value = "maxNodes", defaultValue = "50") int maxNodes) {
        
        log.info("Received PPI network request for: {}, depth: {}, maxNodes: {}", 
                geneSymbol, depth, maxNodes);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (geneSymbol == null || geneSymbol.trim().isEmpty()) {
                response.put("code", 400);
                response.put("msg", "Gene symbol cannot be empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> network = ppiService.getGeneNetwork(
                    geneSymbol.toUpperCase(), 
                    Math.min(depth, 3), 
                    Math.min(maxNodes, 100));
            
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", network);
            
            log.info("PPI network retrieved successfully for: {}", geneSymbol);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving PPI network for {}: {}", geneSymbol, e.getMessage(), e);
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
