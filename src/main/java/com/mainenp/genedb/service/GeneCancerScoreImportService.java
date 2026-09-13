package com.mainenp.genedb.service;

import com.mainenp.genedb.entity.Gene;
import com.mainenp.genedb.entity.GeneCancerScore;
import com.mainenp.genedb.repository.GeneCancerScoreRepository;
import com.mainenp.genedb.repository.GeneRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@Transactional
public class GeneCancerScoreImportService {
    
    private final GeneCancerScoreRepository geneCancerScoreRepository;
    private final GeneRepository geneRepository;
    
    public GeneCancerScoreImportService(GeneCancerScoreRepository geneCancerScoreRepository,
                                        GeneRepository geneRepository) {
        this.geneCancerScoreRepository = geneCancerScoreRepository;
        this.geneRepository = geneRepository;
    }
    
    /**
     * 从 CSV 文件导入癌症分数数据
     * CSV 格式: gene_symbol, score, rank, tissue_type (可选)
     * 
     * 例如:
     * ASCL1,-1.0048750133333333,1,Lung
     * HNF1B,-1.178686975,2,Lung
     */
    @Async
    public void importCancerScoresFromCSV(MultipartFile file, String cancerType) {
        log.info("开始导入 {} 的癌症分数数据...", cancerType);
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            List<GeneCancerScore> scoresToSave = new ArrayList<>();
            AtomicInteger processedCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 跳过空行和表头
                if (line.trim().isEmpty() || lineNumber == 1) {
                    continue;
                }
                
                try {
                    GeneCancerScore cancerScore = parseCancerScoreLine(line, cancerType);
                    if (cancerScore != null) {
                        scoresToSave.add(cancerScore);
                        
                        // 每 5000 条记录批量保存一次
                        if (scoresToSave.size() >= 5000) {
                            geneCancerScoreRepository.saveAll(scoresToSave);
                            log.info("已保存 {} 条分数记录", processedCount.addAndGet(scoresToSave.size()));
                            scoresToSave.clear();
                        }
                    }
                } catch (Exception e) {
                    log.warn("第 {} 行处理失败: {}", lineNumber, line, e);
                    errorCount.incrementAndGet();
                }
            }
            
            // 保存剩余的分数
            if (!scoresToSave.isEmpty()) {
                geneCancerScoreRepository.saveAll(scoresToSave);
                processedCount.addAndGet(scoresToSave.size());
            }
            
            log.info("癌症分数导入完成。成功: {}, 失败: {}", processedCount.get(), errorCount.get());
        } catch (Exception e) {
            log.error("导入癌症分数失败", e);
        }
    }
    
    /**
     * 解析单行癌症分数数据
     */
    private GeneCancerScore parseCancerScoreLine(String line, String cancerType) {
        String[] parts = line.split(",");
        
        if (parts.length < 2) {
            log.warn("列数不足: {}", line);
            return null;
        }
        
        try {
            String geneSymbol = parts[0].trim().toUpperCase();
            Double score = Double.parseDouble(parts[1].trim());
            Integer rank = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : null;
            String tissueType = parts.length > 3 ? parts[3].trim() : cancerType;
            
            // 查找基因
            Gene gene = geneRepository.findBySymbol(geneSymbol)
                .orElseGet(() -> createMinimalGene(geneSymbol));
            
            // 创建癌症分数对象
            GeneCancerScore cancerScore = new GeneCancerScore();
            cancerScore.setGene(gene);
            cancerScore.setCancerType(cancerType);
            cancerScore.setTissueType(tissueType);
            cancerScore.setScore(score);
            cancerScore.setRanking(rank);
            cancerScore.setCreatedAt(LocalDate.now());
            cancerScore.setPredictionDate(LocalDate.now());
            
            return cancerScore;
        } catch (NumberFormatException e) {
            log.warn("数字解析失败: {}", line, e);
            return null;
        }
    }
    
    /**
     * 创建最小化的基因对象
     */
    private Gene createMinimalGene(String symbol) {
        Gene gene = new Gene();
        gene.setSymbol(symbol);
        gene.setName(symbol);
        gene.setDescription("Auto-created from cancer score import");
        return geneRepository.save(gene);
    }
    
    /**
     * 获取癌症分数统计信息
     */
    public Map<String, Object> getCancerScoreStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalScores = geneCancerScoreRepository.count();
        List<String> cancerTypes = geneCancerScoreRepository.findDistinctCancerTypes();
        
        stats.put("totalScores", totalScores);
        stats.put("cancerTypes", cancerTypes);
        stats.put("timestamp", System.currentTimeMillis());
        
        // 按癌症类型统计
        Map<String, Long> scoresByCancerType = new HashMap<>();
        for (String cancerType : cancerTypes) {
            long count = geneCancerScoreRepository.countByCancerType(cancerType);
            scoresByCancerType.put(cancerType, count);
        }
        stats.put("scoresByCancerType", scoresByCancerType);
        
        return stats;
    }
    
    /**
     * 获取特定基因的所有癌症分数
     */
    public List<GeneCancerScore> getGeneScoresBySymbol(String geneSymbol) {
        return geneCancerScoreRepository.findByGeneSymbol(geneSymbol);
    }
    
    /**
     * 获取特定癌症的所有基因分数
     */
    public List<GeneCancerScore> getScoresByCancerType(String cancerType) {
        return geneCancerScoreRepository.findByCancerType(cancerType);
    }
    
    /**
     * 删除特定癌症类型的所有分数（用于重新导入）
     */
    @Transactional
    public void deleteCancerTypeScores(String cancerType) {
        List<GeneCancerScore> scores = geneCancerScoreRepository.findByCancerType(cancerType);
        geneCancerScoreRepository.deleteAll(scores);
        log.info("已删除 {} 的所有分数记录", cancerType);
    }
}
