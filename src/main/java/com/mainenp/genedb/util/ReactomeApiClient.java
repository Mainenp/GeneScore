package com.mainenp.genedb.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Reactome API Client
 * Retrieves gene pathway information from Reactome database
 */
@Slf4j
@Component
public class ReactomeApiClient {
    
    private static final String REACTOME_API_URL = "https://reactome.org/ContentService/data/query/Homo%20sapiens/entity/Ensembl";
    private final GeneApiClient apiClient;
    
    public ReactomeApiClient(GeneApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Get Reactome pathways for a gene
     * @param ensemblId the Ensembl gene ID
     * @return JSON response containing pathway data
     */
    public String getPathways(String ensemblId) {
        try {
            if (ensemblId == null || ensemblId.isEmpty()) {
                log.warn("Empty ensemblId provided for Reactome pathways");
                return "{\"error\": \"Empty ensemblId\"}";
            }
            String url = REACTOME_API_URL + ":" + ensemblId + "/pathways";
            log.info("Fetching pathways from Reactome for gene: {}", ensemblId);
            return apiClient.get(url);
        } catch (Exception e) {
            log.error("Failed to fetch pathways from Reactome for gene {}: {}", ensemblId, e.getMessage());
            return "{\"error\": \"Failed to fetch Reactome pathways\"}";
        }
    }
}
