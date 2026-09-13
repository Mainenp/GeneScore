package com.mainenp.genedb.service;

import com.mainenp.genedb.entity.Gene;
import com.mainenp.genedb.entity.GeneScore;
import com.mainenp.genedb.repository.GeneRepository;
import com.mainenp.genedb.repository.GeneScoreRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
@Service
@Slf4j
@Transactional
public class GeneDataImportService {
    
    private final GeneRepository geneRepository;
    private final GeneScoreRepository geneScoreRepository;
    private final RestTemplate restTemplate;
    
    public GeneDataImportService(GeneRepository geneRepository,
                                 GeneScoreRepository geneScoreRepository,
                                 RestTemplate restTemplate) {
        this.geneRepository = geneRepository;
        this.geneScoreRepository = geneScoreRepository;
        this.restTemplate = restTemplate;
    }
    
    /**
     * 从 Ensembl API 导入所有人类基因
     * 这是一个长时间运行的操作，应该异步执行
     */
    @Async
    public void importAllGenesFromEnsembl() {
        log.info("开始从 Ensembl 导入所有人类基因...");
        log.warn("注意: Ensembl 不提供批量获取所有基因的 API，请使用其他方式导入基因列表");
    }
    
    /**
     * 从 CSV 文件导入基因分数
     * CSV 格式: gene_symbol,cancer_type,score,tissue_type
     */
    @Async
    public void importGeneScoresFromCSV(List<String> csvLines, String datasetName) {
        log.info("开始导入基因分数，数据集: {}", datasetName);
        
        List<GeneScore> scoresToSave = new ArrayList<>();
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        for (String line : csvLines) {
            try {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    errorCount.incrementAndGet();
                    continue;
                }
                
                String geneSymbol = parts[0].trim().toUpperCase();
                String cancerType = parts[1].trim();
                Double score = Double.parseDouble(parts[2].trim());
                String tissueType = parts.length > 3 ? parts[3].trim() : cancerType;
                
                // 查找或创建基因
                Gene gene = geneRepository.findBySymbol(geneSymbol)
                    .orElseGet(() -> createMinimalGene(geneSymbol));
                
                GeneScore geneScore = new GeneScore();
                geneScore.setGene(gene);
                geneScore.setModelPredictionScore(score);
                geneScore.setCancerType(cancerType);
                geneScore.setTissueType(tissueType);
                geneScore.setDataset(datasetName);
                geneScore.setDependencyScore(score); // 兼容旧字段
                geneScore.setLineage(tissueType); // 兼容旧字段
                
                scoresToSave.add(geneScore);
                
                // 每 5000 条记录批量保存一次
                if (scoresToSave.size() >= 5000) {
                    geneScoreRepository.saveAll(scoresToSave);
                    log.info("已保存 {} 条分数记录", processedCount.addAndGet(scoresToSave.size()));
                    scoresToSave.clear();
                }
            } catch (Exception e) {
                log.warn("处理分数行失败: {}", line, e);
                errorCount.incrementAndGet();
            }
        }
        
        // 保存剩余的分数
        if (!scoresToSave.isEmpty()) {
            geneScoreRepository.saveAll(scoresToSave);
            processedCount.addAndGet(scoresToSave.size());
        }
        
        log.info("基因分数导入完成。成功: {}, 失败: {}", processedCount.get(), errorCount.get());
    }
    
    /**
     * 为现有基因补充 Ensembl 信息
     */
    @Async
    public void enrichGenesWithEnsemblData() {
        log.info("开始补充基因 Ensembl 信息...");
        
        List<Gene> allGenes = geneRepository.findAll();
        List<Gene> genesToUpdate = new ArrayList<>();
        
        for (Gene gene : allGenes) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> ensemblData = restTemplate.getForObject(
                    "https://rest.ensembl.org/lookup/symbol/homo_sapiens/" + gene.getSymbol() + "?expand=1",
                    Map.class
                );
                
                if (ensemblData != null) {
                    if (ensemblData.containsKey("seq_region_name")) {
                        gene.setChromosome((String) ensemblData.get("seq_region_name"));
                    }
                    if (ensemblData.containsKey("start")) {
                        gene.setStartPosition((Long) ensemblData.get("start"));
                    }
                    if (ensemblData.containsKey("end")) {
                        gene.setEndPosition((Long) ensemblData.get("end"));
                    }
                    if (ensemblData.containsKey("biotype")) {
                        gene.setBiotype((String) ensemblData.get("biotype"));
                    }
                    
                    genesToUpdate.add(gene);
                    
                    if (genesToUpdate.size() >= 500) {
                        geneRepository.saveAll(genesToUpdate);
                        log.info("已更新 {} 个基因", genesToUpdate.size());
                        genesToUpdate.clear();
                    }
                }
            } catch (Exception e) {
                log.warn("补充基因信息失败: {}", gene.getSymbol(), e);
            }
        }
        
        if (!genesToUpdate.isEmpty()) {
            geneRepository.saveAll(genesToUpdate);
            log.info("已更新 {} 个基因", genesToUpdate.size());
        }
        
        log.info("基因信息补充完成");
    }
    
    /**
     * 创建最小化的基因对象（仅包含必要字段）
     */
    private Gene createMinimalGene(String symbol) {
        Gene gene = new Gene();
        gene.setSymbol(symbol);
        gene.setName(symbol);
        gene.setDescription("Auto-created from score import");
        return geneRepository.save(gene);
    }
    
    /**
     * 获取导入统计信息
     */
    public Map<String, Object> getImportStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGenes", geneRepository.count());
        stats.put("totalScores", geneScoreRepository.count());
        
        // 按数据集统计
        List<Object[]> scoresByDataset = geneScoreRepository.countByDataset();
        Map<String, Long> datasetStats = new HashMap<>();
        for (Object[] row : scoresByDataset) {
            datasetStats.put((String) row[0], (Long) row[1]);
        }
        stats.put("scoresByDataset", datasetStats);
        
        return stats;
    }
}
