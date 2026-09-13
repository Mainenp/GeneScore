package com.mainenp.genedb.controller;

import com.mainenp.genedb.service.GeneEnrichmentService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/genes/enrich")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class GeneEnrichmentController {
    
    private final GeneEnrichmentService geneEnrichmentService;
    
    public GeneEnrichmentController(GeneEnrichmentService geneEnrichmentService) {
        this.geneEnrichmentService = geneEnrichmentService;
    }
    
    /**
     * 从 CSV 文件提取基因列表并创建基因记录
     * POST /api/genes/enrich/extract-and-create
     * 
     * 参数:
     * - file: CSV 文件（第一列为基因名称，格式: GENE_NAME (EntrezID)）
     */
    @PostMapping("/extract-and-create")
    public ResponseEntity<Map<String, Object>> extractAndCreateGenes(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("开始处理文件: {}", file.getOriginalFilename());
            
            // 读取 CSV 文件
            List<String> csvLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    csvLines.add(line);
                }
            }
            
            // 提取基因符号
            List<String> geneSymbols = geneEnrichmentService.extractGeneSymbolsFromCSV(csvLines);
            
            if (geneSymbols.isEmpty()) {
                response.put("status", "error");
                response.put("message", "未能从 CSV 中提取任何基因");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建基因记录
            geneEnrichmentService.createGeneRecords(geneSymbols);
            
            response.put("status", "success");
            response.put("message", "基因记录创建已启动");
            response.put("extractedGenes", geneSymbols.size());
            response.put("geneList", geneSymbols.subList(0, Math.min(10, geneSymbols.size())));
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("处理失败", e);
            response.put("status", "error");
            response.put("message", "处理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 从公共 API 获取基因信息并富集数据库
     * POST /api/genes/enrich/from-public-apis
     * 
     * 参数:
     * - file: CSV 文件（包含基因列表）
     */
    @PostMapping("/from-public-apis")
    public ResponseEntity<Map<String, Object>> enrichFromPublicAPIs(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("开始从公共 API 富集基因信息: {}", file.getOriginalFilename());
            
            // 读取 CSV 文件
            List<String> csvLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    csvLines.add(line);
                }
            }
            
            // 提取基因符号
            List<String> geneSymbols = geneEnrichmentService.extractGeneSymbolsFromCSV(csvLines);
            
            if (geneSymbols.isEmpty()) {
                response.put("status", "error");
                response.put("message", "未能从 CSV 中提取任何基因");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 异步从公共 API 获取信息
            geneEnrichmentService.enrichGenesWithPublicData(geneSymbols);
            
            response.put("status", "success");
            response.put("message", "基因信息富集已启动（异步处理）");
            response.put("genesCount", geneSymbols.size());
            response.put("apis", new String[]{
                "NCBI Gene",
                "UniProt",
                "RCSB PDB",
                "Ensembl"
            });
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("富集失败", e);
            response.put("status", "error");
            response.put("message", "富集失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 从资源文件夹的 CSV 提取基因并创建记录
     * POST /api/genes/enrich/from-resources
     */
    @PostMapping("/from-resources")
    public ResponseEntity<Map<String, Object>> enrichFromResources() {
        log.info("开始从资源文件夹提取基因...");
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 读取资源文件
            ClassLoader classLoader = getClass().getClassLoader();
            java.io.InputStream inputStream = classLoader.getResourceAsStream(
                    "gene.csv");
            
            if (inputStream == null) {
                response.put("status", "error");
                response.put("message", "找不到资源文件: gene.csv");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 读取 CSV 文件
            List<String> csvLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    csvLines.add(line);
                }
            }
            
            // 提取基因符号
            List<String> geneSymbols = geneEnrichmentService.extractGeneSymbolsFromCSV(csvLines);
            
            if (geneSymbols.isEmpty()) {
                response.put("status", "error");
                response.put("message", "未能从 CSV 中提取任何基因");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建基因记录
            geneEnrichmentService.createGeneRecords(geneSymbols);
            
            // 异步从公共 API 获取信息
            geneEnrichmentService.enrichGenesWithPublicData(geneSymbols);
            
            response.put("status", "success");
            response.put("message", "基因提取和富集已启动");
            response.put("source", "resources/gene.csv");
            response.put("extractedGenes", geneSymbols.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("处理失败", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取基因富集统计信息
     * GET /api/genes/enrich/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getEnrichmentStatistics() {
        try {
            Map<String, Object> stats = geneEnrichmentService.getEnrichmentStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
