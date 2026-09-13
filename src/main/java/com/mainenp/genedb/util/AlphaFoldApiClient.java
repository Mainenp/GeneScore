package com.mainenp.genedb.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;



/**
 * AlphaFold DB API Client
 * Retrieves 3D protein structure predictions
 */
@Slf4j
@Component
public class AlphaFoldApiClient {
    
    private static final String ALPHAFOLD_API_URL = "https://alphafold.ebi.ac.uk/api/prediction";
    private final GeneApiClient apiClient;
    
    public AlphaFoldApiClient(GeneApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Get 3D structure prediction for a gene using UniProt ID
     * @param uniprotId the UniProt accession number
     * @return JSON response containing structure data
     */
    public String get3DStructure(String uniprotId) {
        try {
            if (uniprotId == null || uniprotId.trim().isEmpty()) {
                log.warn("No UniProt ID provided for AlphaFold");
                return "{}";
            }
            String url = ALPHAFOLD_API_URL + "/" + uniprotId;
            log.info("Fetching 3D structure from AlphaFold for UniProt ID: {}", uniprotId);
            return apiClient.get(url);
        } catch (Exception e) {
            log.error("Failed to fetch 3D structure for UniProt ID {}: {}", uniprotId, e.getMessage());
            return "{}";
        }
    }
}
