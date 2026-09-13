package com.mainenp.genedb.controller;

import com.mainenp.genedb.dto.GeneAnalysisResponseDTO;
import com.mainenp.genedb.service.GeneAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gene/analysis")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"})
public class GeneAnalysisController {

    @Autowired
    private GeneAnalysisService geneAnalysisService;

    /**
     * Get comprehensive gene analysis data from Python microservice
     * GET /api/gene/analysis/{geneSymbol}?cancer_type={cancerType}
     *
     * @param geneSymbol gene symbol (path parameter)
     * @param cancerType cancer type (query parameter, optional, default: LUAD)
     * @return unified response with analysis data
     */
    @GetMapping("/{geneSymbol}")
    public ResponseEntity<Map<String, Object>> getGeneAnalysis(
            @PathVariable String geneSymbol,
            @RequestParam(name = "cancer_type", required = false, defaultValue = "LUAD") String cancerType) {

        log.info("Received gene analysis request for: {} with cancer type: {}", geneSymbol, cancerType);

        Map<String, Object> response = new HashMap<>();

        try {
            if (geneSymbol == null || geneSymbol.trim().isEmpty()) {
                response.put("code", 400);
                response.put("msg", "Gene symbol cannot be empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            GeneAnalysisResponseDTO analysisData = geneAnalysisService.getGeneAnalysis(
                    geneSymbol.toUpperCase(),
                    cancerType
            );

            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", analysisData);

            log.info("Gene analysis data retrieved successfully for: {}", geneSymbol);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving gene analysis for {}: {}", geneSymbol, e.getMessage(), e);
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
