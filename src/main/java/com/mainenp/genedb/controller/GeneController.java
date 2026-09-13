package com.mainenp.genedb.controller;

import com.mainenp.genedb.dto.*;
import com.mainenp.genedb.entity.GeneScoreDb;
import com.mainenp.genedb.service.GeneService;
import com.mainenp.genedb.service.GeneScoreDbImportService;
import com.mainenp.genedb.service.PpiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/genes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class GeneController {
    
    @Autowired
    private GeneService geneService;
    
    @Autowired
    private GeneScoreDbImportService geneScoreDbImportService;
    
    @Autowired
    private PpiService ppiService;
    
    @GetMapping("/search")
    public List<Map<String, String>> searchGenes(@RequestParam String query) {
        return geneService.searchGenes(query);
    }
    
    @GetMapping("/{symbol}/overview")
    public GeneOverviewDTO getGeneOverview(@PathVariable String symbol) {
        return geneService.getGeneOverview(symbol);
    }
    
    @GetMapping("/{symbol}/body-map")
    public List<BodyMapDataDTO> getBodyMapData(@PathVariable String symbol) {
        return geneService.getBodyMapData(symbol);
    }
    
    @GetMapping("/{symbol}/sankey")
    public SankeyDTO getSankeyData(@PathVariable String symbol) {
        return geneService.getSankeyData(symbol);
    }
    
    @GetMapping("/{symbol}/analytics")
    public AnalyticsDTO getAnalyticsData(@PathVariable String symbol) {
        return geneService.getAnalyticsData(symbol);
    }
    
    @GetMapping("/{symbol}/co-dependencies")
    public List<Map<String, Object>> getCoDependencyNetwork(@PathVariable String symbol) {
        return geneService.getCoDependencyNetwork(symbol);
    }
    
    @GetMapping("/{symbol}/lung-score")
    public com.mainenp.genedb.dto.LungGeneScoreDTO getLungGeneScore(@PathVariable String symbol) {
        return geneService.getLungGeneScore(symbol);
    }
    
    @GetMapping("/cancer-types")
    public List<String> getCancerTypes() {
        return List.of("Lung Cancer", "Breast Cancer", "Colon Cancer", "Prostate Cancer", "Liver Cancer", "Stomach Cancer");
    }
    
    @GetMapping("/scores")
    public List<Map<String, Object>> getGeneScores(@RequestParam String cancerType) {
        return geneService.getGeneScoresByType(cancerType);
    }
    
    @GetMapping("/scores-by-symbols")
    public List<Map<String, Object>> getGeneScoresBySymbols(
            @RequestParam String cancerType,
            @RequestParam String symbols) {
        List<String> symbolList = Arrays.asList(symbols.split(","));
        return geneService.getGeneScoresBySymbols(cancerType, symbolList);
    }
    
    @GetMapping("/{symbol}/gene-score-db")
    public Map<String, Double> getGeneScoreDbScores(@PathVariable String symbol) {
        GeneScoreDb geneScoreDb = geneScoreDbImportService.getGeneScoreDb(symbol);
        if (geneScoreDb == null) {
            return new HashMap<>();
        }

        Map<String, Double> scores = new HashMap<>();
        scores.put("Brain", geneScoreDb.getBrain());
        scores.put("Lung", geneScoreDb.getLung());
        scores.put("Breast", geneScoreDb.getBreast());
        scores.put("Liver", geneScoreDb.getLiver());
        scores.put("Stomach", geneScoreDb.getStomach());
        scores.put("Pancreas", geneScoreDb.getPancreas());
        scores.put("Bowel", geneScoreDb.getBowel());
        scores.put("Kidney", geneScoreDb.getKidney());
        scores.put("Ovary", geneScoreDb.getOvary());
        scores.put("Prostate", geneScoreDb.getProstate());
        scores.put("Skin", geneScoreDb.getSkin());

        return scores;
    }

    /**
     * 手动触发 gene_score_db.csv 导入（用于调试）
     */
    @PostMapping("/gene-score-db/import")
    public Map<String, Object> triggerImport() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("手动触发 gene_score_db.csv 导入...");
            geneScoreDbImportService.importGeneScoreDbFromResources();
            result.put("success", true);
            result.put("message", "导入任务已启动");
        } catch (Exception e) {
            log.error("导入失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 检查 gene_score_db 数据状态
     */
    @GetMapping("/gene-score-db/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            boolean hasData = geneScoreDbImportService.hasData();
            status.put("hasData", hasData);
            status.put("success", true);
        } catch (Exception e) {
            log.error("检查状态失败", e);
            status.put("success", false);
            status.put("error", e.getMessage());
        }
        return status;
    }

    /**
     * 获取基因的 PPI 网络数据
     */
    @GetMapping("/{symbol}/network")
    public Map<String, Object> getGeneNetwork(@PathVariable String symbol) {
        return ppiService.getGeneNetwork(symbol, 1, 50);
    }
}