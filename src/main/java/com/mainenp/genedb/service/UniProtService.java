package com.mainenp.genedb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UniProtService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String UNIPROT_API = "https://rest.uniprot.org/uniprotkb";
    
    public UniProtService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 从UniProt获取基因的蛋白质信息和PDB ID
     */
    public Map<String, Object> getProteinInfo(String uniprotId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String url = UNIPROT_API + "/" + uniprotId + ".json";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            // 提取基本信息
            result.put("uniprotId", uniprotId);
            result.put("proteinName", root.path("proteinDescription").path("recommendedName").path("fullName").path("value").asText());
            result.put("organism", root.path("organism").path("scientificName").asText());
            
            // 提取PDB ID
            JsonNode references = root.path("uniProtKBCrossReferences");
            for (JsonNode ref : references) {
                if ("PDB".equals(ref.path("database").asText())) {
                    result.put("pdbId", ref.path("id").asText());
                    break;
                }
            }
            
            // 提取GO terms
            JsonNode goTerms = root.path("uniProtKBCrossReferences");
            for (JsonNode ref : goTerms) {
                if ("GO".equals(ref.path("database").asText())) {

                    String goTerm = ref.path("properties").get(0).path("value").asText();
                    if (goTerm.contains("biological_process")) {
                        result.put("biologicalProcess", goTerm);
                    } else if (goTerm.contains("molecular_function")) {
                        result.put("molecularFunction", goTerm);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error fetching UniProt data for {}: {}", uniprotId, e.getMessage());
        }
        return result;
    }
}
