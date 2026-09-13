package com.mainenp.genedb.controller;

import com.mainenp.genedb.service.LungSpecificCSVImportService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/genes/import/lung")
@Slf4j
@CrossOrigin(origins = "*")
public class LungSpecificImportController {
    
    private final LungSpecificCSVImportService lungSpecificImportService;
    
    public LungSpecificImportController(LungSpecificCSVImportService lungSpecificImportService) {
        this.lungSpecificImportService = lungSpecificImportService;
    }
    
    /**
     * 上传并导入 LUNG_SPECIFIC CSV 文件
     * POST /api/genes/import/lung/upload
     * 
     * 参数:
     * - file: CSV 文件 (gene.csv)
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadLungSpecificCSV(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("开始处理 LUNG_SPECIFIC 文件: {}", file.getOriginalFilename());
            
            // 异步导入数据
            lungSpecificImportService.importLungSpecificData(file);
            
            response.put("status", "success");
            response.put("message", "LUNG_SPECIFIC 数据导入已启动");
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
     * 从资源文件夹导入 LUNG_SPECIFIC 数据
     * POST /api/genes/import/lung/from-resources
     */
    @PostMapping("/from-resources")
    public ResponseEntity<Map<String, Object>> importFromResources() {
        log.info("开始从资源文件夹导入 LUNG_SPECIFIC 数据...");
        
        Map<String, Object> response = new HashMap<>();
        try {
            lungSpecificImportService.importLungSpecificFromResources();
            response.put("status", "success");
            response.put("message", "LUNG_SPECIFIC 数据导入已启动");
            response.put("source", "resources/gene.csv");
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
     * 获取 LUNG_SPECIFIC 导入统计信息
     * GET /api/genes/import/lung/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getLungSpecificStatistics() {
        try {
            Map<String, Object> stats = lungSpecificImportService.getLungSpecificStatistics();
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
