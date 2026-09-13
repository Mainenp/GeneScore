package com.mainenp.genedb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class EnsemblService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String ENSEMBL_API = "https://rest.ensembl.org";
    
    public EnsemblService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Get genomic coordinates and transcript information from Ensembl
     */
    public Map<String, Object> getGenomicInfo(String geneSymbol) {
        Map<String, Object> genomicInfo = new HashMap<>();
        try {
            String url = ENSEMBL_API + "/lookup/symbol/homo_sapiens/" + geneSymbol + "?expand=1";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            genomicInfo.put("ensemblId", root.path("id").asText());
            genomicInfo.put("chromosome", root.path("seq_region_name").asText());
            genomicInfo.put("start", root.path("start").asLong());
            genomicInfo.put("end", root.path("end").asLong());
            genomicInfo.put("strand", root.path("strand").asInt());
            genomicInfo.put("biotype", root.path("biotype").asText());
            
            // Extract transcript information
            List<Map<String, Object>> transcripts = new ArrayList<>();
            JsonNode transcriptNodes = root.path("Transcript");
            if (transcriptNodes.isArray()) {
                for (JsonNode transcript : transcriptNodes) {
                    Map<String, Object> txInfo = new HashMap<>();
                    txInfo.put("transcriptId", transcript.path("id").asText());
                    txInfo.put("biotype", transcript.path("biotype").asText());
                    txInfo.put("length", transcript.path("length").asLong());
                    transcripts.add(txInfo);
                }
            }
            genomicInfo.put("transcripts", transcripts);
            
        } catch (Exception e) {
            log.error("Error fetching genomic info for {}: {}", geneSymbol, e.getMessage());
        }
        return genomicInfo;
    }
    
    /**
     * Get orthologs from Ensembl
     */
    public List<Map<String, Object>> getOrthologs(String geneSymbol) {
        List<Map<String, Object>> orthologs = new ArrayList<>();
        try {
            String url = ENSEMBL_API + "/homology/symbol/homo_sapiens/" + geneSymbol + "?type=orthologues&format=json";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            JsonNode data = root.path("data");
            if (data.isArray() && data.size() > 0) {
                JsonNode homologies = data.get(0).path("homologies");
                if (homologies.isArray()) {
                    for (JsonNode homology : homologies) {
                        Map<String, Object> ortholog = new HashMap<>();
                        ortholog.put("id", homology.path("id").asText());
                        ortholog.put("species", homology.path("species").asText());
                        ortholog.put("type", homology.path("type").asText());
                        ortholog.put("percentId", homology.path("perc_id").asDouble());
                        orthologs.add(ortholog);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching orthologs for {}: {}", geneSymbol, e.getMessage());
        }
        return orthologs;
    }
    
    /**
     * Get paralogs from Ensembl
     */
    public List<Map<String, Object>> getParalogs(String geneSymbol) {
        List<Map<String, Object>> paralogs = new ArrayList<>();
        try {
            String url = ENSEMBL_API + "/homology/symbol/homo_sapiens/" + geneSymbol + "?type=paralogues&format=json";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            JsonNode data = root.path("data");
            if (data.isArray() && data.size() > 0) {
                JsonNode homologies = data.get(0).path("homologies");
                if (homologies.isArray()) {
                    for (JsonNode homology : homologies) {
                        Map<String, Object> paralog = new HashMap<>();
                        paralog.put("id", homology.path("id").asText());
                        paralog.put("symbol", homology.path("target").path("perc_id").asText());
                        paralog.put("type", homology.path("type").asText());
                        paralog.put("percentId", homology.path("perc_id").asDouble());
                        paralogs.add(paralog);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching paralogs for {}: {}", geneSymbol, e.getMessage());
        }
        return paralogs;
    }
}
