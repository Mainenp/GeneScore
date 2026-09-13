package com.mainenp.genedb.controller;

import com.mainenp.genedb.service.GeneDataImportService;
import com.mainenp.genedb.service.GeneScoreCSVParserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/genes/import")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class GeneImportController {
    
    private final GeneDataImportService geneDataImportService;
    private final GeneScoreCSVParserService csvParserService;
    
    public GeneImportController(GeneDataImportService geneDataImportService,
                               GeneScoreCSVParserService csvParserService) {
        this.geneDataImportService = geneDataImportService;
        this.csvParserService = csvParserService;
    }
    
    /**
     * 从 Ensembl 导入所有人类基因
     * POST /api/genes/import/ensembl
     */
    @PostMapping("/ensembl")
    public ResponseEntity<Map<String, Object>> importFromEnsembl() {
        log.info("开始从 Ensembl 导入基因...");
        
        Map<String, Object> response = new HashMap<>();
        try {
            geneDataImportService.importAllGenesFromEnsembl();
            response.put("status", "success");
            response.put("message", "基因导入已启动，请稍候...");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("导入失败", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 上传并导入 CSV 文件中的基因分数
     * POST /api/genes/import/scores
     * 
     * 参数:
     * - file: CSV 文件
     * - datasetName: 数据集名称（可选，默认为文件名）
     */
    @PostMapping("/scores")
    public ResponseEntity<Map<String, Object>> importScoresFromCSV(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "datasetName", required = false) String datasetName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 解析 CSV 文件
            List<String> csvLines = csvParserService.parseCSVFile(file);
            
            if (csvLines.isEmpty()) {
                response.put("status", "error");
                response.put("message", "CSV 文件中没有有效数据");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证数据质量
            Map<String, Object> qualityReport = csvParserService.validateDataQuality(csvLines);
            
            if (!(boolean) qualityReport.get("isValid")) {
                response.put("status", "error");
                response.put("message", "数据质量检查失败");
                response.put("qualityReport", qualityReport);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取统计信息
            Map<String, Object> stats = csvParserService.getCSVStatistics(csvLines);
            
            // 使用文件名作为数据集名称（如果未指定）
            if (datasetName == null || datasetName.isEmpty()) {
                datasetName = file.getOriginalFilename()
                    .replaceAll("\\.csv$", "")
                    .replaceAll("[^a-zA-Z0-9_-]", "_");
            }
            
            // 异步导入数据
            geneDataImportService.importGeneScoresFromCSV(csvLines, datasetName);
            
            response.put("status", "success");
            response.put("message", "分数导入已启动");
            response.put("datasetName", datasetName);
            response.put("statistics", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("文件读取失败", e);
            response.put("status", "error");
            response.put("message", "文件读取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("导入失败", e);
            response.put("status", "error");
            response.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 验证 CSV 文件格式
     * POST /api/genes/import/validate-csv
     */
    @PostMapping("/validate-csv")
    public ResponseEntity<Map<String, Object>> validateCSV(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<String> csvLines = csvParserService.parseCSVFile(file);
            Map<String, Object> stats = csvParserService.getCSVStatistics(csvLines);
            Map<String, Object> qualityReport = csvParserService.validateDataQuality(csvLines);
            
            response.put("status", "success");
            response.put("statistics", stats);
            response.put("qualityReport", qualityReport);
            response.put("columnCount", csvParserService.detectColumnCount(csvLines));
            response.put("isValid", qualityReport.get("isValid"));
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("文件验证失败", e);
            response.put("status", "error");
            response.put("message", "文件验证失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 为现有基因补充 Ensembl 信息
     * POST /api/genes/import/enrich
     */
    @PostMapping("/enrich")
    public ResponseEntity<Map<String, Object>> enrichGenesWithEnsemblData() {
        log.info("开始补充基因信息...");
        
        Map<String, Object> response = new HashMap<>();
        try {
            geneDataImportService.enrichGenesWithEnsemblData();
            response.put("status", "success");
            response.put("message", "基因信息补充已启动");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("补充失败", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取导入统计信息
     * GET /api/genes/import/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getImportStatistics() {
        try {
            Map<String, Object> stats = geneDataImportService.getImportStatistics();
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
