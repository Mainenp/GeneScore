package com.mainenp.genedb.service;

import com.mainenp.genedb.entity.GeneScoreDb;
import com.mainenp.genedb.repository.GeneScoreDbRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GeneScoreDbImportService {

    private final GeneScoreDbRepository geneScoreDbRepository;

    public GeneScoreDbImportService(GeneScoreDbRepository geneScoreDbRepository) {
        this.geneScoreDbRepository = geneScoreDbRepository;
    }

    /**
     * 从 gene_score_db.csv 导入数据到 gene_score_db 表
     */
    @Transactional
    public void importGeneScoreDbFromResources() {
        log.info("开始从资源文件夹导入 gene_score_db.csv 数据...");

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            java.io.InputStream inputStream = classLoader.getResourceAsStream(
                    "genescore/gene_score_db.csv");

            if (inputStream == null) {
                log.error("找不到资源文件: genescore/gene_score_db.csv");
                return;
            }

            List<GeneScoreDb> allScores = new ArrayList<>();
            int lineNumber = 0;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;

                    // 跳过表头
                    if (lineNumber == 1) {
                        continue;
                    }

                    // 跳过空行
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    try {
                        GeneScoreDb geneScoreDb = parseLine(line);
                        if (geneScoreDb != null) {
                            allScores.add(geneScoreDb);
                        }
                    } catch (Exception e) {
                        log.warn("第 {} 行处理失败: {}", lineNumber, line, e);
                    }
                }

                log.info("解析完成，共 {} 条记录准备保存", allScores.size());

                // 清空表然后批量导入（防止重复）
                geneScoreDbRepository.deleteAll();
                log.info("已清空旧数据");

                // 分批次保存
                int batchSize = 1000;
                for (int i = 0; i < allScores.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, allScores.size());
                    List<GeneScoreDb> batch = allScores.subList(i, end);
                    geneScoreDbRepository.saveAll(batch);
                    log.info("已保存 {}/{} 条记录", end, allScores.size());
                }

                log.info("gene_score_db.csv 导入完成，共保存 {} 条记录", allScores.size());

            } catch (Exception e) {
                log.error("导入过程出错", e);
            }

        } catch (Exception e) {
            log.error("从资源文件导入失败", e);
        }
    }

    /**
     * 解析单行数据
     * CSV格式: Gene_Symbol,Bowel,Breast,Brain,Stomach,Head and Neck,Kidney,Liver,Lung,Ovary,Pancreas,Prostate,Skin
     */
    private GeneScoreDb parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 13) { // 需要 13 列（基因名 + 12个组织）
            return null;
        }

        GeneScoreDb geneScoreDb = new GeneScoreDb();
        geneScoreDb.setGeneSymbol(parts[0].trim().toUpperCase());

        try {
            geneScoreDb.setBowel(parseDouble(parts[1]));
            geneScoreDb.setBreast(parseDouble(parts[2]));
            geneScoreDb.setBrain(parseDouble(parts[3]));
            geneScoreDb.setStomach(parseDouble(parts[4]));
            geneScoreDb.setHeadAndNeck(parseDouble(parts[5]));
            geneScoreDb.setKidney(parseDouble(parts[6]));
            geneScoreDb.setLiver(parseDouble(parts[7]));
            geneScoreDb.setLung(parseDouble(parts[8]));
            geneScoreDb.setOvary(parseDouble(parts[9]));
            geneScoreDb.setPancreas(parseDouble(parts[10]));
            geneScoreDb.setProstate(parseDouble(parts[11]));
            geneScoreDb.setSkin(parseDouble(parts[12]));
        } catch (Exception e) {
            log.debug("解析数值时出错: {}", e.getMessage());
        }

        return geneScoreDb;
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取基因分数数据
     */
    public GeneScoreDb getGeneScoreDb(String geneSymbol) {
        return geneScoreDbRepository.findByGeneSymbolIgnoreCase(geneSymbol).orElse(null);
    }

    /**
     * 检查是否已导入数据
     */
    public boolean hasData() {
        try {
            return geneScoreDbRepository.count() > 0;
        } catch (Exception e) {
            log.error("检查数据是否存在时出错", e);
            return false;
        }
    }
}
