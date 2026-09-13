package com.mainenp.genedb.controller;

import com.mainenp.genedb.dto.SynergyResponseDto;
import com.mainenp.genedb.dto.SynergyResultDto;
import com.mainenp.genedb.service.SynergyService;
import com.mainenp.genedb.util.HeatmapGenerator;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@RestController
@RequestMapping("/api/dko")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Slf4j
@SuppressWarnings("unchecked")
public class DkoController {

    private final RestTemplate restTemplate;
    private final SynergyService synergyService;
    
    public DkoController(RestTemplate restTemplate, SynergyService synergyService) {
        this.restTemplate = restTemplate;
        this.synergyService = synergyService;
    }

    @Value("${python.microservice.url:http://localhost:8000}")
    private String pythonServiceUrl;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeDko(@RequestBody Map<String, Object> request) {
        List<String> genes = (List<String>) request.get("genes");

        if (genes == null || genes.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Genes list is required");
            errorResponse.put("status", "error");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            log.info("Processing DKO analyze request for genes: {}", genes);

            // 1. 调用新的 Python 接口 /api/predict_synergy
            String genesParam = String.join(",", genes);
            String synergyUrl = UriComponentsBuilder
                    .fromUriString(pythonServiceUrl + "/api/predict_synergy")
                    .queryParam("genes", genesParam)
                    .build()
                    .toUriString();

            log.info("Calling Python service at: {}", synergyUrl);
            SynergyResponseDto synergyResponse = restTemplate.getForObject(synergyUrl, SynergyResponseDto.class);

            if (synergyResponse == null || synergyResponse.getResults() == null) {
                throw new RuntimeException("Invalid response from Python service");
            }

            log.info("Received synergy response with {} pairs", synergyResponse.getPairs_tested());

            // 2. 构建分数矩阵
            List<String> validGenes = extractValidGenes(synergyResponse);
            Map<String, Map<String, Double>> scores = buildScoreMatrix(validGenes, synergyResponse.getResults());

            // 3. 生成热图
            String imageBase64 = HeatmapGenerator.generateHeatmapBase64(validGenes, scores);

            // 4. 构建兼容旧格式的响应
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("valid_genes", validGenes);
            response.put("scores", scores);
            response.put("image_base64", imageBase64);

            log.info("DKO analyze successful for genes: {}", genes);
            return ResponseEntity.ok(response);

        } catch (RestClientException e) {
            log.error("Failed to connect to Python microservice at {}: {}", pythonServiceUrl, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Python microservice unavailable");
            errorResponse.put("status", "error");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(503).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during DKO analyze: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("status", "error");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private List<String> extractValidGenes(SynergyResponseDto response) {
        Set<String> geneSet = new LinkedHashSet<>();
        for (SynergyResultDto result : response.getResults()) {
            geneSet.add(result.getGene_A());
            geneSet.add(result.getGene_B());
        }
        return new ArrayList<>(geneSet);
    }

    private Map<String, Map<String, Double>> buildScoreMatrix(List<String> genes, List<SynergyResultDto> results) {
        Map<String, Map<String, Double>> scores = new HashMap<>();

        // 初始化矩阵
        for (String gene1 : genes) {
            scores.put(gene1, new HashMap<>());
            for (String gene2 : genes) {
                scores.get(gene1).put(gene2, 0.0);
            }
        }

        // 填充分数
        for (SynergyResultDto result : results) {
            String geneA = result.getGene_A();
            String geneB = result.getGene_B();
            double score = result.getPredicted_synergy();

            if (scores.containsKey(geneA) && scores.get(geneA).containsKey(geneB)) {
                scores.get(geneA).put(geneB, score);
            }
            if (scores.containsKey(geneB) && scores.get(geneB).containsKey(geneA)) {
                scores.get(geneB).put(geneA, score);
            }
        }

        return scores;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkPythonServiceHealth() {
        try {
            restTemplate.getForEntity(pythonServiceUrl + "/health", String.class);
            Map<String, String> result = new HashMap<>();
            result.put("status", "healthy");
            result.put("pythonService", "connected");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "unhealthy");
            result.put("pythonService", "disconnected");
            result.put("error", e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    @GetMapping("/predict_synergy")
    public ResponseEntity<?> predictSynergy(@RequestParam String genes) {
        try {
            if (genes == null || genes.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Genes parameter is required");
                errorResponse.put("status", "error");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("Processing synergy prediction for genes: {}", genes);
            SynergyResponseDto response = synergyService.predictSynergy(genes);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during synergy prediction: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Synergy prediction failed");
            errorResponse.put("status", "error");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
