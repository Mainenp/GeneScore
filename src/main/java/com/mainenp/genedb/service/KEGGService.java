package com.mainenp.genedb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Service
@Slf4j
public class KEGGService {
    private final RestTemplate restTemplate;
    
    private static final String KEGG_API = "https://rest.kegg.jp";
    
    public KEGGService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Get pathway associations for a gene from KEGG
     */
    public List<Map<String, Object>> getPathwayAssociations(String geneSymbol, String entrezId) {
        List<Map<String, Object>> pathways = new ArrayList<>();
        try {
            // Search for human gene
            String searchUrl = KEGG_API + "/find/genes/" + geneSymbol + "/homo_sapiens";
            String searchResponse = restTemplate.getForObject(searchUrl, String.class);
            
            if (searchResponse == null || searchResponse.isEmpty()) {
                return pathways;
            }
            
            // Extract KEGG gene ID from search results
            String[] lines = searchResponse.split("\n");
            if (lines.length == 0) {
                return pathways;
            }
            
            String keggGeneId = lines[0].split("\t")[0];
            
            // Get pathways for this gene
            String pathwayUrl = KEGG_API + "/link/pathway/" + keggGeneId;
            String pathwayResponse = restTemplate.getForObject(pathwayUrl, String.class);
            
            if (pathwayResponse == null || pathwayResponse.isEmpty()) {
                return pathways;
            }
            
            // Parse pathway results
            String[] pathwayLines = pathwayResponse.split("\n");
            Set<String> pathwayIds = new HashSet<>();
            
            for (String line : pathwayLines) {
                if (line.contains("path:")) {
                    String pathwayId = line.split("\t")[1];
                    pathwayIds.add(pathwayId);
                }
            }
            
            // Get pathway details
            for (String pathwayId : pathwayIds) {
                String detailUrl = KEGG_API + "/get/" + pathwayId;
                String detailResponse = restTemplate.getForObject(detailUrl, String.class);
                
                if (detailResponse != null) {
                    Map<String, Object> pathway = parsePathwayDetail(pathwayId, detailResponse);
                    pathways.add(pathway);
                }
            }
            
        } catch (Exception e) {
            log.error("Error fetching pathway associations for {}: {}", geneSymbol, e.getMessage());
        }
        return pathways;
    }
    
    /**
     * Parse pathway detail information
     */
    private Map<String, Object> parsePathwayDetail(String pathwayId, String response) {
        Map<String, Object> pathway = new HashMap<>();
        pathway.put("id", pathwayId);
        
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.startsWith("NAME")) {
                pathway.put("name", line.substring(12).trim());
            } else if (line.startsWith("DESCRIPTION")) {
                pathway.put("description", line.substring(12).trim());
            } else if (line.startsWith("CLASS")) {
                pathway.put("class", line.substring(12).trim());
            }
        }
        
        return pathway;
    }
    
    /**
     * Get disease associations from KEGG
     */
    public List<Map<String, Object>> getDiseaseAssociations(String geneSymbol, String entrezId) {
        List<Map<String, Object>> diseases = new ArrayList<>();
        try {
            String url = KEGG_API + "/link/disease/" + entrezId;
            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null || response.isEmpty()) {
                return diseases;
            }
            
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.contains("H")) {
                    String[] parts = line.split("\t");
                    if (parts.length >= 2) {
                        Map<String, Object> disease = new HashMap<>();
                        disease.put("id", parts[1]);
                        disease.put("name", parts[1].replace("H", ""));
                        diseases.add(disease);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching disease associations for {}: {}", geneSymbol, e.getMessage());
        }
        return diseases;
    }
}
