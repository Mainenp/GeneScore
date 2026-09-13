package com.mainenp.genedb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PpiService {

    // 存储PPI数据: gene -> List<interactingGenes>
    private final Map<String, Set<String>> ppiData = new ConcurrentHashMap<>();
    
    // 存储所有PPI对（双向）
    private final Set<String> allInteractions = new HashSet<>();

    @PostConstruct
    public void init() {
        loadPpiData();
    }

    /**
     * 从ppi.csv加载PPI数据
     */
    public void loadPpiData() {
        log.info("开始加载ppi.csv数据...");
        
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            java.io.InputStream inputStream = classLoader.getResourceAsStream("genescore/ppi.csv");
            
            if (inputStream == null) {
                log.error("找不到资源文件: genescore/ppi.csv");
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                
                String line;
                int lineNumber = 0;
                int interactionCount = 0;

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

                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String gene1 = parts[0].trim().toUpperCase();
                        String gene2 = parts[1].trim().toUpperCase();
                        
                        if (!gene1.isEmpty() && !gene2.isEmpty() && !gene1.equals(gene2)) {
                            addInteraction(gene1, gene2);
                            interactionCount++;
                        }
                    }
                }
                
                log.info("ppi.csv加载完成，共{}个交互关系，涉及{}个基因", 
                        interactionCount, ppiData.size());
                
            } catch (Exception e) {
                log.error("加载ppi.csv失败", e);
            }
            
        } catch (Exception e) {
            log.error("初始化PPI数据失败", e);
        }
    }

    /**
     * 添加双向交互
     */
    private void addInteraction(String gene1, String gene2) {
        ppiData.computeIfAbsent(gene1, k -> new HashSet<>()).add(gene2);
        ppiData.computeIfAbsent(gene2, k -> new HashSet<>()).add(gene1);
        
        // 确保只记录一次交互（按字母顺序）
        String key = gene1.compareTo(gene2) < 0 ? gene1 + "_" + gene2 : gene2 + "_" + gene1;
        allInteractions.add(key);
    }

    /**
     * 获取基因的PPI网络数据
     * @param geneSymbol 查询的基因
     * @param maxDepth 最大深度（默认为1）
     * @param maxNodes 最大节点数（默认为50）
     */
    public Map<String, Object> getGeneNetwork(String geneSymbol, int maxDepth, int maxNodes) {
        String gene = geneSymbol.toUpperCase();
        
        if (!ppiData.containsKey(gene)) {
            log.warn("基因{}未在PPI数据中找到", geneSymbol);
            return createEmptyNetwork(gene);
        }

        Set<String> nodes = new HashSet<>();
        List<Map<String, Object>> links = new ArrayList<>();
        
        nodes.add(gene);
        
        // BFS方式构建网络
        Queue<String> queue = new LinkedList<>();
        queue.add(gene);
        
        Set<String> visited = new HashSet<>();
        visited.add(gene);
        
        int currentDepth = 0;
        
        while (!queue.isEmpty() && currentDepth < maxDepth && nodes.size() < maxNodes) {
            int levelSize = queue.size();
            
            for (int i = 0; i < levelSize && nodes.size() < maxNodes; i++) {
                String current = queue.poll();
                Set<String> interactors = ppiData.getOrDefault(current, Collections.emptySet());
                
                for (String interactor : interactors) {
                    if (!visited.contains(interactor) && nodes.size() < maxNodes) {
                        nodes.add(interactor);
                        visited.add(interactor);
                        
                        if (currentDepth < maxDepth - 1) {
                            queue.add(interactor);
                        }
                    }
                    
                    // 添加边（仅当两个节点都已收集时）
                    if (nodes.contains(interactor)) {
                        Map<String, Object> link = new HashMap<>();
                        link.put("source", current);
                        link.put("target", interactor);
                        link.put("type", "interacts");
                        links.add(link);
                    }
                }
            }
            
            currentDepth++;
        }

        // 构建节点列表
        List<Map<String, Object>> nodeList = new ArrayList<>();
        for (String n : nodes) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", n);
            node.put("label", n);
            node.put("type", n.equals(gene) ? "query" : "interactor");
            nodeList.add(node);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodeList);
        result.put("links", links);
        
        log.info("为基因{}构建PPI网络，包含{}个节点，{}条边", gene, nodeList.size(), links.size());
        
        return result;
    }

    /**
     * 获取基因的直接交互基因
     */
    public Set<String> getInteractors(String geneSymbol) {
        return ppiData.getOrDefault(geneSymbol.toUpperCase(), Collections.emptySet());
    }

    /**
     * 检查两个基因是否有交互
     */
    public boolean hasInteraction(String gene1, String gene2) {
        String g1 = gene1.toUpperCase();
        String g2 = gene2.toUpperCase();
        String key = g1.compareTo(g2) < 0 ? g1 + "_" + g2 : g2 + "_" + g1;
        return allInteractions.contains(key);
    }

    /**
     * 获取PPI数据中的所有基因
     */
    public Set<String> getAllGenes() {
        return Collections.unmodifiableSet(ppiData.keySet());
    }

    /**
     * 创建空网络
     */
    private Map<String, Object> createEmptyNetwork(String gene) {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node = new HashMap<>();
        node.put("id", gene);
        node.put("label", gene);
        node.put("type", "query");
        nodes.add(node);
        
        result.put("nodes", nodes);
        result.put("links", Collections.emptyList());
        return result;
    }
}
