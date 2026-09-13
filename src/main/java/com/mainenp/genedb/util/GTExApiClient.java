package com.mainenp.genedb.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * GTEx API Client
 * Retrieves multi-omics expression data across tissues
 */
@Slf4j
@Component
public class GTExApiClient {
    
    private static final String GTEX_API_URL = "https://gtexportal.org/api/v2/expression/geometricMean";
    private final GeneApiClient apiClient;
    
    public GTExApiClient(GeneApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Get gene expression data across tissues
     * @param entrezId the Entrez gene ID
     * @return JSON response containing expression data
     */
    public String getExpressionData(String entrezId) {
        try {
            if (entrezId == null || entrezId.isEmpty()) {
                log.warn("Empty entrezId provided for GTEx expression data");
                return "{\"error\": \"Empty entrezId\"}";
            }
            Map<String, String> params = new HashMap<>();
            params.put("geneId", entrezId);
            String url = GeneApiClient.buildUrl(GTEX_API_URL, params);
            log.info("Fetching expression data from GTEx for gene: {}", entrezId);
            return apiClient.get(url);
        } catch (Exception e) {
            log.error("Failed to fetch expression data for gene {}: {}", entrezId, e.getMessage());
            return "{\"error\": \"Failed to fetch expression data\"}";
        }
    }
}
