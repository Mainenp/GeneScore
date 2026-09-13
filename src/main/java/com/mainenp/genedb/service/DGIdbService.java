package com.mainenp.genedb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class DGIdbService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String DGIDB_API = "https://www.dgidb.org/api/v2";
    
    public DGIdbService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 从DGIdb获取靶向该基因的药物
     */
    public List<Map<String, Object>> getDrugsForGene(String geneSymbol) {
        List<Map<String, Object>> drugs = new ArrayList<>();
        try {
            String url = DGIDB_API + "/interactions?genes=" + geneSymbol;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            JsonNode matchedGenes = root.path("matchedTerms");
            if (matchedGenes.isArray()) {
                for (JsonNode gene : matchedGenes) {
                    JsonNode interactions = gene.path("interactions");
                    if (interactions.isArray()) {
                        for (JsonNode interaction : interactions) {
                            Map<String, Object> drug = new HashMap<>();
                            drug.put("drugName", interaction.path("drugName").asText());
                            drug.put("interactionType", interaction.path("interactionType").asText());
                            drug.put("source", interaction.path("source").asText());
                            drug.put("pmids", interaction.path("pmids").asText());
                            drugs.add(drug);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching DGIdb data for {}: {}", geneSymbol, e.getMessage());
        }
        return drugs;
    }
}
