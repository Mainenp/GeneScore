package com.mainenp.genedb.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * KEGG API Client
 * Retrieves gene pathway information
 */
@Slf4j
@Component
public class KEGGApiClient {
    
    private static final String KEGG_API_URL = "https://rest.kegg.jp/link/pathway/hsa";
    private final GeneApiClient apiClient;
    
    public KEGGApiClient(GeneApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Get KEGG pathways for a gene
     * @param entrezId the Entrez gene ID
     * @return JSON response containing pathway data
     */
    public String getPathways(String entrezId) {
        try {
            String url = KEGG_API_URL + ":" + entrezId;
            log.info("Fetching pathways from KEGG for gene: {}", entrezId);
            String response = apiClient.get(url);
            return convertKeggToJson(response);
        } catch (Exception e) {
            log.error("Failed to fetch pathways from KEGG for gene {}: {}", entrezId, e.getMessage());
            return "{\"error\": \"Failed to fetch KEGG pathways\"}";
        }
    }
    
    /**
     * Convert KEGG response to JSON format
     * @param keggData KEGG formatted data
     * @return JSON formatted data
     */
    private String convertKeggToJson(String keggData) {
        try {
            String[] lines = keggData.split("\n");
            StringBuilder json = new StringBuilder("{\"pathways\":[");
            
            for (int i = 0; i < lines.length; i++) {
                String[] parts = lines[i].split("\t");
                if (parts.length >= 2) {
                    json.append("{\"id\":\"").append(parts[0]).append("\",")
                            .append("\"name\":\"").append(parts[1]).append("\"}");
                    if (i < lines.length - 1) {
                        json.append(",");
                    }
                }
            }
            json.append("]}");
            return json.toString();
        } catch (Exception e) {
            log.error("Error converting KEGG response to JSON: {}", e.getMessage());
            return "{\"pathways\":[]}";
        }
    }
}
