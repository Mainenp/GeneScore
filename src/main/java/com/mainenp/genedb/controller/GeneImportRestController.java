package com.mainenp.genedb.controller;

import com.mainenp.genedb.util.GeneDataImportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Gene Data Import Controller
 * Provides endpoints to trigger gene data import process
 */
@Slf4j
@RestController
@RequestMapping("/api/import")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, maxAge = 3600)
public class GeneImportRestController {
    
    @Autowired
    private GeneDataImportUtil geneDataImportUtil;
    
    /**
     * Trigger gene data import
     * POST /api/import/genes
     * Reads custom gene list CSV and HGNC data, then imports into database
     * @return unified response with import status
     */
    @PostMapping("/genes")
    public ResponseEntity<Map<String, Object>> importGeneData() {
        
        log.info("Received gene data import request");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            geneDataImportUtil.importGeneData();
            
            response.put("code", 200);
            response.put("msg", "Gene data import completed successfully");
            response.put("data", Map.of("status", "completed"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during gene data import: {}", e.getMessage(), e);
            response.put("code", 500);
            response.put("msg", "Gene data import failed: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
