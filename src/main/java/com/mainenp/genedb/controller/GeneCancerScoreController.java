package com.mainenp.genedb.controller;

import com.mainenp.genedb.entity.GeneCancerScore;
import com.mainenp.genedb.service.GeneCancerScoreImportService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/genes/cancer-scores")
@Slf4j
@CrossOrigin(origins = "*")
public class GeneCancerScoreController {
    
    private final GeneCancerScoreImportService geneCancerScoreImportService;
    
    public GeneCancerScoreController(GeneCancerScoreImportService geneCancerScoreImportService) {
        this.geneCancerScoreImportService = geneCancerScoreImportService;
    }
    
    /**
     * 导入癌症分数数据
     * POST /api/genes/cancer-scores/import
     * 
     * 参数:
     * - file: CSV 文件（格式: gene_symbol, score, rank, tissue_type）
     * - cancerType: 癌症类型（如 Lung, Breast）
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCancerScores(
            @RequestParam("file") MultipartFile file,
            @RequestParam("cancerType") String cancerType) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (cancerType == null || cancerType.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "癌症类型不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("开始导入 {} 的癌症分数: {}", cancerType, file.getOriginalFilename());
            
            // 异步导入数据
            geneCancerScoreImportService.importCancerScoresFromCSV(file, cancerType);
            
            response.put("status", "success");
            response.put("message", "癌症分数导入已启动");
            response.put("cancerType", cancerType);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("导入失败", e);
            response.put("status", "error");
            response.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取癌症分数统计信息
     * GET /api/genes/cancer-scores/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCancerScoreStatistics() {
        try {
            Map<String, Object> stats = geneCancerScoreImportService.getCancerScoreStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取特定基因的所有癌症分数
     * GET /api/genes/{symbol}/cancer-scores
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<Map<String, Object>> getGeneScores(@PathVariable String symbol) {
        try {
            List<GeneCancerScore> scores = geneCancerScoreImportService.getGeneScoresBySymbol(symbol);
            
            Map<String, Object> response = new HashMap<>();
            response.put("symbol", symbol);
            response.put("scores", scores);
            response.put("count", scores.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取基因分数失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取特定癌症类型的所有基因分数
     * GET /api/genes/cancer-scores/by-type/{cancerType}
     */
    @GetMapping("/by-type/{cancerType}")
    public ResponseEntity<Map<String, Object>> getScoresByCancerType(@PathVariable String cancerType) {
        try {
            List<GeneCancerScore> scores = geneCancerScoreImportService.getScoresByCancerType(cancerType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("cancerType", cancerType);
            response.put("scores", scores);
            response.put("count", scores.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取癌症分数失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除特定癌症类型的所有分数（用于重新导入）
     * DELETE /api/genes/cancer-scores/by-type/{cancerType}
     */
    @DeleteMapping("/by-type/{cancerType}")
    public ResponseEntity<Map<String, Object>> deleteCancerTypeScores(@PathVariable String cancerType) {
        try {
            geneCancerScoreImportService.deleteCancerTypeScores(cancerType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "已删除 " + cancerType + " 的所有分数");
            response.put("cancerType", cancerType);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
