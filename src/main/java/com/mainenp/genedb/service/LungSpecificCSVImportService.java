package com.mainenp.genedb.service;

import com.mainenp.genedb.entity.Gene;
import com.mainenp.genedb.entity.GeneScore;
import com.mainenp.genedb.repository.GeneRepository;
import com.mainenp.genedb.repository.GeneScoreRepository;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@Transactional
public class LungSpecificCSVImportService {
    
    private final GeneRepository geneRepository;
    private final GeneScoreRepository geneScoreRepository;
    
    // CSV 列索引
    private static final int GENE_NAME_COL = 0;
    private static final int GLOBAL_SCORE_COL = 1;
    private static final int SELECTIVE_SCORE_COL = 2;
    private static final int DRIVER_DELTA_COL = 3;

    
    public LungSpecificCSVImportService(GeneRepository geneRepository,
                                        GeneScoreRepository geneScoreRepository) {
        this.geneRepository = geneRepository;
        this.geneScoreRepository = geneScoreRepository;
    }
    
    /**
     * 从 LUNG_SPECIFIC CSV 文件导入数据
     * 格式: Gene_Name (EntrezID), Global_Score, Selective_Score, Driver_Delta, Rank
     * 
     * 例如: ASCL1 (429),-0.1579616971983471,-1.0048750133333333,0.8469133161349862,1.0
     */
    @Async
    public void importLungSpecificData(MultipartFile file) {
        log.info("开始导入 LUNG_SPECIFIC 数据...");
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            List<GeneScore> scoresToSave = new ArrayList<>();
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
                    GeneScore geneScore = parseLungSpecificLine(line);
                    if (geneScore != null) {
                        scoresToSave.add(geneScore);
                        
                        // 每 5000 条记录批量保存一次
                        if (scoresToSave.size() >= 5000) {
                            geneScoreRepository.saveAll(scoresToSave);
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
                geneScoreRepository.saveAll(scoresToSave);
                processedCount.addAndGet(scoresToSave.size());
            }
            
            log.info("LUNG_SPECIFIC 数据导入完成。成功: {}, 失败: {}", 
                processedCount.get(), errorCount.get());
        } catch (Exception e) {
            log.error("导入 LUNG_SPECIFIC 数据失败", e);
        }
    }
    
    /**
     * 解析单行 LUNG_SPECIFIC 数据
     */
    private GeneScore parseLungSpecificLine(String line) {
        String[] parts = line.split(",");
        
        if (parts.length < 5) {
            log.warn("列数不足: {}", line);
            return null;
        }
        
        try {
            // 提取基因符号（去掉括号内的 Entrez ID）
            String geneNameWithId = parts[GENE_NAME_COL].trim();
            String geneSymbol = extractGeneSymbol(geneNameWithId);
            String entrezId = extractEntrezId(geneNameWithId);
            
            // 提取分数
            Double globalScore = Double.parseDouble(parts[GLOBAL_SCORE_COL].trim());
            Double selectiveScore = Double.parseDouble(parts[SELECTIVE_SCORE_COL].trim());
            Double driverDelta = Double.parseDouble(parts[DRIVER_DELTA_COL].trim());
            // Double rank = Double.parseDouble(parts[RANK_COL].trim());
            
            // 查找或创建基因
            Gene gene = geneRepository.findBySymbol(geneSymbol)
                .orElseGet(() -> createGeneFromLungData(geneSymbol, entrezId));
            
            // 创建 GeneScore 对象
            GeneScore geneScore = new GeneScore();
            geneScore.setGene(gene);
            geneScore.setModelPredictionScore(selectiveScore); // 使用 Selective Score 作为主要预测分数
            geneScore.setCancerType("Lung");
            geneScore.setTissueType("Lung");
            geneScore.setDataset("LUNG_SPECIFIC");
            geneScore.setLineage("Lung");
            geneScore.setDiseaseSubtype("Lung Cancer");
            geneScore.setDependencyScore(selectiveScore); // 兼容旧字段
            geneScore.setConfidenceScore(Math.abs(driverDelta)); // 使用 Driver Delta 作为置信度
            geneScore.setPredictionDate(LocalDate.now());
            
            // 添加额外的分数信息作为表达水平
            geneScore.setExpressionLevel(globalScore);
            geneScore.setCopyNumber(driverDelta);
            
            return geneScore;
        } catch (NumberFormatException e) {
            log.warn("数字解析失败: {}", line, e);
            return null;
        }
    }
    
    /**
     * 从 "GENE_NAME (ENTREZ_ID)" 格式中提取基因符号
     */
    private String extractGeneSymbol(String geneNameWithId) {
        // 匹配 "GENE_NAME (ID)" 格式
        Pattern pattern = Pattern.compile("^([A-Za-z0-9\\-_]+)\\s*\\(\\d+\\)$");
        Matcher matcher = pattern.matcher(geneNameWithId.trim());
        
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        
        // 如果格式不匹配，直接返回（去掉括号部分）
        int parenIndex = geneNameWithId.indexOf('(');
        if (parenIndex > 0) {
            return geneNameWithId.substring(0, parenIndex).trim().toUpperCase();
        }
        
        return geneNameWithId.trim().toUpperCase();
    }
    
    /**
     * 从 "GENE_NAME (ENTREZ_ID)" 格式中提取 Entrez ID
     */
    private String extractEntrezId(String geneNameWithId) {
        Pattern pattern = Pattern.compile("\\((\\d+)\\)");
        Matcher matcher = pattern.matcher(geneNameWithId);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * 从 LUNG_SPECIFIC 数据创建基因对象
     */
    private Gene createGeneFromLungData(String symbol, String entrezId) {
        Gene gene = new Gene();
        gene.setSymbol(symbol);
        gene.setName(symbol);
        gene.setEntrezId(entrezId);
        gene.setDescription("Auto-created from LUNG_SPECIFIC import");
        gene.setChromosome("Unknown");
        return geneRepository.save(gene);
    }
    
    /**
     * 获取导入统计信息
     */
    public Map<String, Object> getLungSpecificStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalScores = geneScoreRepository.countByDataset("LUNG_SPECIFIC");
        long totalGenes = geneRepository.count();
        
        stats.put("totalGenes", totalGenes);
        stats.put("lungSpecificScores", totalScores);
        stats.put("timestamp", System.currentTimeMillis());
        
        return stats;
    }
    
    /**
     * 从资源文件夹导入 LUNG_SPECIFIC 数据
     */
    @Async
    public void importLungSpecificFromResources() {
        log.info("开始从资源文件夹导入 LUNG_SPECIFIC 数据...");
        
        try {
            // 读取资源文件
            ClassLoader classLoader = getClass().getClassLoader();
            java.io.InputStream inputStream = classLoader.getResourceAsStream(
                    "gene.csv");
            
            if (inputStream == null) {
                log.error("找不到资源文件: gene.csv");
                return;
            }
            
            List<GeneScore> scoresToSave = new ArrayList<>();
            AtomicInteger processedCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                
                String line;
                int lineNumber = 0;
                
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    
                    // 跳过空行和表头
                    if (line.trim().isEmpty() || lineNumber == 1) {
                        continue;
                    }
                    
                    try {
                        GeneScore geneScore = parseLungSpecificLine(line);
                        if (geneScore != null) {
                            scoresToSave.add(geneScore);
                            
                            if (scoresToSave.size() >= 5000) {
                                geneScoreRepository.saveAll(scoresToSave);
                                log.info("已保存 {} 条分数记录", processedCount.addAndGet(scoresToSave.size()));
                                scoresToSave.clear();
                            }
                        }
                    } catch (Exception e) {
                        log.warn("第 {} 行处理失败: {}", lineNumber, line, e);
                        errorCount.incrementAndGet();
                    }
                }
                
                if (!scoresToSave.isEmpty()) {
                    geneScoreRepository.saveAll(scoresToSave);
                    processedCount.addAndGet(scoresToSave.size());
                }
                
                log.info("从资源文件导入完成。成功: {}, 失败: {}", 
                    processedCount.get(), errorCount.get());
            }
        } catch (Exception e) {
            log.error("从资源文件导入失败", e);
        }
    }
}
