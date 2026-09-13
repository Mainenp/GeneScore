package com.mainenp.genedb.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * DGIdb API Client
 * Retrieves drug-gene interaction data
 */
@Slf4j
@Component
public class DGIdbApiClient {
    
    private static final String DGIDB_API_URL = "https://dgidb.org/api/v2/interactions.json";
    private final GeneApiClient apiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public DGIdbApiClient(GeneApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Get drugs targeting a gene
     * @param geneSymbol the gene symbol
     * @return JSON response containing drug interaction data
     */
    public String getDrugInteractions(String geneSymbol) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("genes", geneSymbol);
            String url = GeneApiClient.buildUrl(DGIDB_API_URL, params);
            log.info("Fetching drug interactions from DGIdb for gene: {}", geneSymbol);
            String response = apiClient.get(url);
            
            // 尝试验证和解析响应
            try {
                objectMapper.readTree(response);
                // 如果是有效的 JSON，直接返回
                return response;
            } catch (Exception e) {
                log.warn("DGIdb returned non-JSON response, returning empty array");
                // 返回有效的空 JSON 数组
                return "[]";
            }
        } catch (Exception e) {
            log.error("Failed to fetch drug interactions for gene {}: {}", geneSymbol, e.getMessage());
            // 返回有效的空 JSON 数组而不是错误对象
            return "[]";
        }
    }
}
