package com.mainenp.genedb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class RCSBPDBService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String RCSB_API = "https://data.rcsb.org/rest/v1/core";

    
    public RCSBPDBService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 根据UniProt ID查找PDB结构
     */
    public List<String> getPDBIdsByUniProtId(String uniprotId) {
        List<String> pdbIds = new ArrayList<>();
        try {
            String url = "https://www.rcsb.org/rest/v1/core/uniprot/" + uniprotId + "/related_structures";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            JsonNode data = root.path("data");
            if (data.isArray()) {
                for (JsonNode item : data) {
                    String pdbId = item.path("identifier").asText();
                    if (!pdbId.isEmpty()) {
                        pdbIds.add(pdbId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching PDB structures for UniProt {}: {}", uniprotId, e.getMessage());
        }
        return pdbIds;
    }
    
    /**
     * 获取PDB结构的详细信息
     */
    public Map<String, Object> getPDBStructureInfo(String pdbId) {
        Map<String, Object> info = new HashMap<>();
        try {
            String url = RCSB_API + "/entry/" + pdbId;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            info.put("pdbId", pdbId);
            info.put("title", root.path("struct").path("title").asText());
            info.put("resolution", root.path("rcsb_entry_info").path("resolution_combined").get(0).asDouble());
            info.put("experimentalMethod", root.path("exptl").get(0).path("method").asText());
            
        } catch (Exception e) {
            log.error("Error fetching PDB structure info for {}: {}", pdbId, e.getMessage());
        }
        return info;
    }
}
