package com.mainenp.genedb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class GeneScoreCSVParserService {
    
    /**
     * 解析 CSV 文件
     * 支持的格式:
     * 1. gene_symbol,score
     * 2. gene_symbol,cancer_type,score
     * 3. gene_symbol,cancer_type,score,tissue_type
     */
    public List<String> parseCSVFile(MultipartFile file) throws IOException {
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 跳过空行和注释行
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                // 验证行格式
                if (validateCSVLine(line)) {
                    lines.add(line);
                } else {
                    log.warn("第 {} 行格式不正确: {}", lineNumber, line);
                }
            }
        }
        
        log.info("成功解析 {} 行数据", lines.size());
        return lines;
    }
    
    /**
     * 验证 CSV 行格式
     */
    private boolean validateCSVLine(String line) {
        String[] parts = line.split(",");
        
        // 至少需要 2 列（基因符号和分数）
        if (parts.length < 2) {
            return false;
        }
        
        // 第一列应该是基因符号（字母和数字）
        String symbol = parts[0].trim();
        if (!symbol.matches("[A-Za-z0-9\\-_]+")) {
            return false;
        }
        
        // 最后一列应该是数字（分数）
        try {
            Double.parseDouble(parts[parts.length - 1].trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 规范化基因符号（转换为大写）
     */
    public String normalizeGeneSymbol(String symbol) {
        return symbol.trim().toUpperCase();
    }
    
    /**
     * 规范化癌症类型名称
     */
    public String normalizeCancerType(String cancerType) {
        return cancerType.trim()
            .replaceAll("\\s+", "_")
            .toLowerCase();
    }
    
    /**
     * 从 CSV 行提取基因符号
     */
    public String extractGeneSymbol(String csvLine) {
        String[] parts = csvLine.split(",");
        return normalizeGeneSymbol(parts[0]);
    }
    
    /**
     * 从 CSV 行提取分数
     */
    public Double extractScore(String csvLine) {
        String[] parts = csvLine.split(",");
        return Double.parseDouble(parts[parts.length - 1].trim());
    }
    
    /**
     * 从 CSV 行提取癌症类型（如果存在）
     */
    public String extractCancerType(String csvLine) {
        String[] parts = csvLine.split(",");
        
        if (parts.length >= 3) {
            return normalizeCancerType(parts[1]);
        }
        
        return "unknown";
    }
    
    /**
     * 从 CSV 行提取组织类型（如果存在）
     */
    public String extractTissueType(String csvLine) {
        String[] parts = csvLine.split(",");
        
        if (parts.length >= 4) {
            return parts[3].trim();
        }
        
        // 如果没有指定组织类型，使用癌症类型
        return extractCancerType(csvLine);
    }
    
    /**
     * 获取 CSV 文件的统计信息
     */
    public Map<String, Object> getCSVStatistics(List<String> csvLines) {
        Map<String, Object> stats = new HashMap<>();
        
        Set<String> uniqueGenes = new HashSet<>();
        Set<String> uniqueCancerTypes = new HashSet<>();
        List<Double> scores = new ArrayList<>();
        
        for (String line : csvLines) {
            try {
                uniqueGenes.add(extractGeneSymbol(line));
                uniqueCancerTypes.add(extractCancerType(line));
                scores.add(extractScore(line));
            } catch (Exception e) {
                log.warn("解析行失败: {}", line, e);
            }
        }
        
        stats.put("totalLines", csvLines.size());
        stats.put("uniqueGenes", uniqueGenes.size());
        stats.put("uniqueCancerTypes", uniqueCancerTypes.size());
        stats.put("cancerTypes", new ArrayList<>(uniqueCancerTypes));
        
        if (!scores.isEmpty()) {
            Collections.sort(scores);
            stats.put("minScore", scores.get(0));
            stats.put("maxScore", scores.get(scores.size() - 1));
            stats.put("avgScore", scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
            stats.put("medianScore", scores.get(scores.size() / 2));
        }
        
        return stats;
    }
    
    /**
     * 按癌症类型分组 CSV 数据
     */
    public Map<String, List<String>> groupByCanerType(List<String> csvLines) {
        Map<String, List<String>> grouped = new HashMap<>();
        
        for (String line : csvLines) {
            String cancerType = extractCancerType(line);
            grouped.computeIfAbsent(cancerType, k -> new ArrayList<>()).add(line);
        }
        
        return grouped;
    }
    
    /**
     * 检测 CSV 文件的列数
     */
    public int detectColumnCount(List<String> csvLines) {
        if (csvLines.isEmpty()) {
            return 0;
        }
        
        String firstLine = csvLines.get(0);
        return firstLine.split(",").length;
    }
    
    /**
     * 验证所有行的列数是否一致
     */
    public boolean validateColumnConsistency(List<String> csvLines) {
        if (csvLines.isEmpty()) {
            return true;
        }
        
        int expectedColumns = detectColumnCount(csvLines);
        
        for (String line : csvLines) {
            int actualColumns = line.split(",").length;
            if (actualColumns != expectedColumns) {
                log.warn("列数不一致: 期望 {}, 实际 {}, 行: {}", 
                    expectedColumns, actualColumns, line);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检测并报告数据质量问题
     */
    public Map<String, Object> validateDataQuality(List<String> csvLines) {
        Map<String, Object> report = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        // 检查列数一致性
        if (!validateColumnConsistency(csvLines)) {
            warnings.add("某些行的列数不一致");
        }
        
        // 检查重复的基因
        Set<String> seenGenes = new HashSet<>();
        for (String line : csvLines) {
            String gene = extractGeneSymbol(line);
            if (seenGenes.contains(gene)) {
                warnings.add("发现重复的基因: " + gene);
            }
            seenGenes.add(gene);
        }
        
        // 检查分数范围
        List<Double> scores = new ArrayList<>();
        for (String line : csvLines) {
            try {
                scores.add(extractScore(line));
            } catch (Exception e) {
                errors.add("无法解析分数: " + line);
            }
        }
        
        if (!scores.isEmpty()) {
            double minScore = Collections.min(scores);
            double maxScore = Collections.max(scores);
            
            if (minScore < -10 || maxScore > 10) {
                warnings.add(String.format("分数范围异常: [%.2f, %.2f]", minScore, maxScore));
            }
        }
        
        report.put("totalLines", csvLines.size());
        report.put("warnings", warnings);
        report.put("errors", errors);
        report.put("isValid", errors.isEmpty());
        
        return report;
    }
}
