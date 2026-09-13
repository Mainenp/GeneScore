package com.mainenp.genedb.service;

import com.mainenp.genedb.dto.*;
import com.mainenp.genedb.entity.*;
import com.mainenp.genedb.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class GeneService {
    private final GeneRepository geneRepository;
    private final GeneScoreRepository geneScoreRepository;
    private final GeneDrugRepository geneDrugRepository;
    private final GenePathwayRepository genePathwayRepository;
    private final CoDependencyRepository coDependencyRepository;
    private final UniProtService uniProtService;
    
    public GeneService(GeneRepository geneRepository,
                      GeneScoreRepository geneScoreRepository,
                      GeneDrugRepository geneDrugRepository,
                      GenePathwayRepository genePathwayRepository,
                      CoDependencyRepository coDependencyRepository,
                      UniProtService uniProtService) {
        this.geneRepository = geneRepository;
        this.geneScoreRepository = geneScoreRepository;
        this.geneDrugRepository = geneDrugRepository;
        this.genePathwayRepository = genePathwayRepository;
        this.coDependencyRepository = coDependencyRepository;
        this.uniProtService = uniProtService;
    }
    
    /**
     * 搜索基因
     */
    public List<Map<String, String>> searchGenes(String query) {
        return geneRepository.searchBySymbolOrName(query)
                .stream()
                .limit(10)
                .map(gene -> {
                    Map<String, String> result = new HashMap<>();
                    result.put("symbol", gene.getSymbol());
                    result.put("name", gene.getName());
                    result.put("id", gene.getId().toString());
                    return result;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取基因概览信息 (缓存10分钟)
     */
    @Cacheable(value = "genes", key = "#symbol")
    public GeneOverviewDTO getGeneOverview(String symbol) {
        Gene gene = geneRepository.findBySymbol(symbol)
                .orElseThrow(() -> new com.mainenp.genedb.exception.ResourceNotFoundException("Gene not found: " + symbol));
        
        GeneOverviewDTO dto = new GeneOverviewDTO();
        dto.setId(gene.getId());
        dto.setSymbol(gene.getSymbol());
        dto.setName(gene.getName());
        dto.setEntrezId(gene.getEntrezId());
        dto.setUniprotId(gene.getUniprotId());
        dto.setDescription(gene.getDescription());
        dto.setPdbId(gene.getPdbId());
        
        // 从UniProt API获取GO terms
        if (gene.getUniprotId() != null) {
            Map<String, Object> proteinInfo = uniProtService.getProteinInfo(gene.getUniprotId());
            dto.setBiologicalProcess((String) proteinInfo.getOrDefault("biologicalProcess", "N/A"));
            dto.setMolecularFunction((String) proteinInfo.getOrDefault("molecularFunction", "N/A"));
        }
        
        return dto;
    }
    
    /**
     * 获取人体图数据（按器官/lineage聚合依赖分数）
     */
    @Cacheable(value = "bodyMapData", key = "#symbol")
    public List<BodyMapDataDTO> getBodyMapData(String symbol) {
        List<GeneScore> scores = geneScoreRepository.findByGeneSymbol(symbol);
        
        Map<String, Double> averageScores = scores.stream()
                .filter(score -> score.getLineage() != null && score.getDependencyScore() != null)
                .collect(Collectors.groupingBy(
                        GeneScore::getLineage,
                        Collectors.averagingDouble(GeneScore::getDependencyScore)
                ));
        
        return averageScores.entrySet().stream()
                .map(entry -> {
                    BodyMapDataDTO dto = new BodyMapDataDTO();
                    dto.setLineage(entry.getKey());
                    dto.setAverageDependencyScore(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取Sankey图数据（疾病 -> 通路 -> 基因 -> 药物）
     */
    @Cacheable(value = "sankeyData", key = "#symbol")
    public SankeyDTO getSankeyData(String symbol) {
        Gene gene = geneRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Gene not found: " + symbol));
        
        SankeyDTO sankeyDTO = new SankeyDTO();
        List<SankeyNodeDTO> nodes = new ArrayList<>();
        List<SankeyLinkDTO> links = new ArrayList<>();
        Set<String> nodeNames = new HashSet<>();
        
        // 添加基因节点
        SankeyNodeDTO geneNode = new SankeyNodeDTO();
        geneNode.setName(gene.getSymbol());
        geneNode.setCategory("Gene");
        nodes.add(geneNode);
        nodeNames.add(gene.getSymbol());
        
        // 添加疾病节点和链接
        List<GeneScore> scores = geneScoreRepository.findByGeneSymbol(symbol);
        Set<String> diseases = scores.stream()
                .map(GeneScore::getDiseaseSubtype)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        for (String disease : diseases) {
            if (!nodeNames.contains(disease)) {
                SankeyNodeDTO diseaseNode = new SankeyNodeDTO();
                diseaseNode.setName(disease);
                diseaseNode.setCategory("Disease");
                nodes.add(diseaseNode);
                nodeNames.add(disease);
            }
            
            SankeyLinkDTO link = new SankeyLinkDTO();
            link.setSource(disease);
            link.setTarget(gene.getSymbol());
            link.setValue(1.0);
            links.add(link);
        }
        
        // 添加通路节点和链接
        List<GenePathway> pathways = genePathwayRepository.findByGeneSymbol(symbol);
        for (GenePathway pathway : pathways) {
            String pathwayName = pathway.getPathwayName();
            if (!nodeNames.contains(pathwayName)) {
                SankeyNodeDTO pathwayNode = new SankeyNodeDTO();
                pathwayNode.setName(pathwayName);
                pathwayNode.setCategory("Pathway");
                nodes.add(pathwayNode);
                nodeNames.add(pathwayName);
            }
            
            SankeyLinkDTO link = new SankeyLinkDTO();
            link.setSource(gene.getSymbol());
            link.setTarget(pathwayName);
            link.setValue(1.0);
            links.add(link);
        }
        
        // 添加药物节点和链接（从DGIdb API获取）
        List<GeneDrug> drugs = geneDrugRepository.findByGeneSymbol(symbol);
        for (GeneDrug drug : drugs) {
            String drugName = drug.getDrugName();
            if (!nodeNames.contains(drugName)) {
                SankeyNodeDTO drugNode = new SankeyNodeDTO();
                drugNode.setName(drugName);
                drugNode.setCategory("Drug");
                nodes.add(drugNode);
                nodeNames.add(drugName);
            }
            
            SankeyLinkDTO link = new SankeyLinkDTO();
            link.setSource(gene.getSymbol());
            link.setTarget(drugName);
            link.setValue(drug.getEvidenceCount() != null ? drug.getEvidenceCount().doubleValue() : 1.0);
            links.add(link);
        }
        
        sankeyDTO.setNodes(nodes);
        sankeyDTO.setLinks(links);
        return sankeyDTO;
    }
    
    /**
     * 获取分析数据（散点图、箱线图等）
     */
    @Cacheable(value = "analyticsData", key = "#symbol")
    public AnalyticsDTO getAnalyticsData(String symbol) {
        List<GeneScore> scores = geneScoreRepository.findByGeneSymbol(symbol);
        
        AnalyticsDTO analyticsDTO = new AnalyticsDTO();
        
        // 散点图数据
        List<ScatterPointDTO> scatterData = scores.stream()
                .filter(score -> score.getExpressionLevel() != null && score.getDependencyScore() != null)
                .map(score -> {
                    ScatterPointDTO point = new ScatterPointDTO();
                    point.setExpressionLevel(score.getExpressionLevel());
                    point.setDependencyScore(score.getDependencyScore());
                    point.setLineage(score.getLineage());
                    return point;
                })
                .collect(Collectors.toList());
        analyticsDTO.setScatterData(scatterData);
        
        // 箱线图数据
        Map<String, List<Double>> lineageScores = scores.stream()
                .filter(score -> score.getDependencyScore() != null)
                .collect(Collectors.groupingBy(
                        GeneScore::getLineage,
                        Collectors.mapping(GeneScore::getDependencyScore, Collectors.toList())
                ));
        
        List<BoxplotDataDTO> boxplotData = lineageScores.entrySet().stream()
                .map(entry -> {
                    BoxplotDataDTO boxplot = new BoxplotDataDTO();
                    boxplot.setLineage(entry.getKey());
                    boxplot.setScores(entry.getValue());
                    return boxplot;
                })
                .collect(Collectors.toList());
        analyticsDTO.setBoxplotData(boxplotData);
        
        return analyticsDTO;
    }
    
    /**
     * 获取共依赖网络数据
     */
    @Cacheable(value = "coDependencies", key = "#symbol")
    public List<Map<String, Object>> getCoDependencyNetwork(String symbol) {
        List<CoDependency> coDeps = coDependencyRepository.findByGeneSymbol(symbol);
        
        return coDeps.stream()
                .map(coDep -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("geneA", coDep.getGeneA().getSymbol());
                    item.put("geneB", coDep.getGeneB().getSymbol());
                    item.put("correlation", coDep.getPearsonCorrelation());
                    item.put("pValue", coDep.getPValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取肺癌特异性基因评分
     */
    @Cacheable(value = "lungGeneScores", key = "#symbol")
    public LungGeneScoreDTO getLungGeneScore(String symbol) {
        List<GeneScore> scores = geneScoreRepository.findByGeneSymbol(symbol);
        
        // 查找 LUNG_SPECIFIC 数据集的评分
        GeneScore lungScore = scores.stream()
                .filter(s -> "LUNG_SPECIFIC".equals(s.getDataset()))
                .findFirst()
                .orElse(null);
        
        if (lungScore == null) {
            return null;
        }
        
        LungGeneScoreDTO dto = new LungGeneScoreDTO();
        dto.setGeneSymbol(symbol);
        dto.setGlobalScore(lungScore.getExpressionLevel());
        dto.setSelectiveScore(lungScore.getDependencyScore());
        dto.setDriverDelta(lungScore.getCopyNumber());
        
        // 从排名推断（如果有的话）
        if (lungScore.getConfidenceScore() != null) {
            dto.setRank(lungScore.getConfidenceScore());
        }
        
        // 计算综合评分
        Double compositeScore = calculateCompositeScore(
                lungScore.getExpressionLevel(),
                lungScore.getDependencyScore(),
                lungScore.getCopyNumber()
        );
        dto.setCompositeScore(compositeScore);
        
        // 确定评分等级
        dto.setScoreLevel(determineScoreLevel(compositeScore));
        
        // 判断是否为驱动基因（基于 Driver Delta）
        dto.setIsDriver(lungScore.getCopyNumber() != null && lungScore.getCopyNumber() > 0.3);
        
        return dto;
    }
    
    /**
     * 计算综合评分
     */
    private Double calculateCompositeScore(Double globalScore, Double selectiveScore, Double driverDelta) {
        if (globalScore == null || selectiveScore == null) {
            return 0.0;
        }
        
        // 综合评分 = (全局评分 + 选择性评分) / 2 + 驱动因子差异权重
        double base = (globalScore + selectiveScore) / 2.0;
        double driverWeight = (driverDelta != null ? driverDelta : 0.0) * 0.3;
        
        return base + driverWeight;
    }
    
    /**
     * 确定评分等级
     */
    private String determineScoreLevel(Double score) {
        if (score == null) {
            return "Unknown";
        }
        
        if (score > 0.5) {
            return "High";
        } else if (score > -0.5) {
            return "Medium";
        } else {
            return "Low";
        }
    }
    
    /**
     * 获取特定癌症类型的基因评分
     */
    public List<Map<String, Object>> getGeneScoresByType(String cancerType) {
        List<GeneScore> scores = geneScoreRepository.findByCancerType(cancerType);
        
        return scores.stream()
                .limit(100)
                .map(score -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", score.getId());
                    item.put("cancerType", score.getCancerType());
                    item.put("geneSymbol", score.getGene().getSymbol());
                    item.put("score", score.getDependencyScore() != null ? score.getDependencyScore() : 0.0);
                    return item;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取特定基因符号的评分
     */
    public List<Map<String, Object>> getGeneScoresBySymbols(String cancerType, List<String> symbols) {
        List<GeneScore> scores = geneScoreRepository.findByCancerType(cancerType);
        
        return scores.stream()
                .filter(score -> symbols.contains(score.getGene().getSymbol()))
                .map(score -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", score.getId());
                    item.put("cancerType", score.getCancerType());
                    item.put("geneSymbol", score.getGene().getSymbol());
                    item.put("score", score.getDependencyScore() != null ? score.getDependencyScore() : 0.0);
                    return item;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取基因的所有组织特异性分数（来自 GENE_SCORE_DB）
     */
    @Cacheable(value = "geneScoreDbScores", key = "#symbol")
    public Map<String, Double> getGeneScoreDbScores(String symbol) {
        List<GeneScore> scores = geneScoreRepository.findByGeneSymbolAndDataset(symbol, "GENE_SCORE_DB");
        
        Map<String, Double> tissueScores = new HashMap<>();
        
        for (GeneScore score : scores) {
            if (score.getTissueType() != null && score.getModelPredictionScore() != null) {
                tissueScores.put(score.getTissueType(), score.getModelPredictionScore());
            }
        }
        
        return tissueScores;
    }
}
