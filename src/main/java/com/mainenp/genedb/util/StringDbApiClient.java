package com.mainenp.genedb.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * STRING DB API Client
 * Retrieves gene interaction networks
 */
@Slf4j
@Component
public class StringDbApiClient {
    
    private static final String STRING_API_URL = "https://string-db.org/api/tsv/network";
    private static final String SPECIES_ID = "9606"; // Homo sapiens
    private static final int REQUIRED_SCORE = 400; // Minimum combined score (0-1000)
    private static final int LIMIT = 10; // Limit number of interactions
    private final GeneApiClient apiClient;
    
    public StringDbApiClient(GeneApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Get gene interaction network
     * @param geneSymbol the gene symbol
     * @return JSON response containing network data
     */
    public String getInteractionNetwork(String geneSymbol) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("identifiers", geneSymbol);
            params.put("species", SPECIES_ID);
            params.put("required_score", String.valueOf(REQUIRED_SCORE));
            params.put("limit", String.valueOf(LIMIT));
            String url = GeneApiClient.buildUrl(STRING_API_URL, params);
            log.info("Fetching interaction network from STRING for gene: {}", geneSymbol);
            log.info("STRING DB URL: {}", url);
            String tsvResponse = apiClient.get(url);
            log.info("Received TSV response from STRING DB, length: {}", tsvResponse.length());
            return convertTsvToJson(tsvResponse);
        } catch (Exception e) {
            log.error("Failed to fetch interaction network for gene {}: {}", geneSymbol, e.getMessage(), e);
            return "[]"; // Return empty array instead of error object for consistency
        }
    }
    
    /**
     * Convert TSV response to JSON format
     * @param tsvData TSV formatted data
     * @return JSON formatted data
     */
    private String convertTsvToJson(String tsvData) {
        try {
            String[] lines = tsvData.split("\n");
            if (lines.length < 2) {
                log.warn("No data in STRING DB response (less than 2 lines)");
                return "[]";
            }
            
            String[] headers = lines[0].split("\t");
            log.info("STRING DB headers: {}", String.join(", ", headers));
            StringBuilder json = new StringBuilder("[");
            
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                
                String[] values = line.split("\t");
                json.append("{");
                for (int j = 0; j < headers.length && j < values.length; j++) {
                    String header = headers[j].trim();
                    String value = values[j].trim();
                    // Escape quotes in value
                    value = value.replace("\"", "\\\"");
                    json.append("\"").append(header).append("\":\"").append(value).append("\"");
                    if (j < headers.length - 1 && j < values.length - 1) {
                        json.append(",");
                    }
                }
                json.append("}");
                if (i < lines.length - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            log.info("Converted STRING DB data to JSON, {} interactions", lines.length - 1);
            return json.toString();
        } catch (Exception e) {
            log.error("Error converting TSV to JSON: {}", e.getMessage(), e);
            return "[]";
        }
    }
}
