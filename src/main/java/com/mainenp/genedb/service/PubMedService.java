package com.mainenp.genedb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class PubMedService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String PUBMED_ESEARCH_API = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi";
    private static final String PUBMED_EFETCH_API = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi";
    
    public PubMedService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Get recent literature references for a gene from PubMed
     */
    public List<Map<String, Object>> getRecentReferences(String geneSymbol, int maxResults) {
        List<Map<String, Object>> references = new ArrayList<>();
        try {
            // Search for recent articles
            String searchUrl = PUBMED_ESEARCH_API + "?db=pubmed&term=" + geneSymbol + 
                    "[TIAB]+AND+(cancer+OR+mutation+OR+expression)&sort=date&retmax=" + maxResults + 
                    "&rettype=json&retmode=json";
            
            String searchResponse = restTemplate.getForObject(searchUrl, String.class);
            JsonNode searchRoot = objectMapper.readTree(searchResponse);
            
            JsonNode idList = searchRoot.path("esearchresult").path("idlist");
            if (!idList.isArray() || idList.size() == 0) {
                return references;
            }
            
            // Collect PMIDs
            List<String> pmids = new ArrayList<>();
            for (JsonNode id : idList) {
                pmids.add(id.asText());
            }
            
            if (pmids.isEmpty()) {
                return references;
            }
            
            // Fetch article details
            String pmidList = String.join(",", pmids);
            String fetchUrl = PUBMED_EFETCH_API + "?db=pubmed&id=" + pmidList + 
                    "&rettype=json&retmode=json";
            
            String fetchResponse = restTemplate.getForObject(fetchUrl, String.class);
            JsonNode fetchRoot = objectMapper.readTree(fetchResponse);
            
            JsonNode articles = fetchRoot.path("result").path("uids");
            if (articles.isArray()) {
                for (JsonNode uid : articles) {
                    String pmid = uid.asText();
                    JsonNode article = fetchRoot.path("result").path(pmid);
                    
                    Map<String, Object> ref = new HashMap<>();
                    ref.put("pmid", pmid);
                    ref.put("title", article.path("title").asText());
                    ref.put("authors", extractAuthors(article));
                    ref.put("journal", article.path("source").asText());
                    ref.put("pubDate", article.path("pubdate").asText());
                    ref.put("abstract", article.path("abstract").asText());
                    ref.put("url", "https://pubmed.ncbi.nlm.nih.gov/" + pmid);
                    
                    references.add(ref);
                }
            }
            
        } catch (Exception e) {
            log.error("Error fetching PubMed references for {}: {}", geneSymbol, e.getMessage());
        }
        return references;
    }
    
    /**
     * Extract author information from article
     */
    private List<String> extractAuthors(JsonNode article) {
        List<String> authors = new ArrayList<>();
        try {
            JsonNode authorList = article.path("authors");
            if (authorList.isArray()) {
                for (JsonNode author : authorList) {
                    String name = author.path("name").asText();
                    if (!name.isEmpty()) {
                        authors.add(name);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error extracting authors: {}", e.getMessage());
        }
        return authors;
    }
    
    /**
     * Get citation count for a gene
     */
    public int getCitationCount(String geneSymbol) {
        try {
            String url = PUBMED_ESEARCH_API + "?db=pubmed&term=" + geneSymbol + 
                    "[TIAB]&rettype=json&retmode=json";
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            return root.path("esearchresult").path("count").asInt();
        } catch (Exception e) {
            log.error("Error fetching citation count for {}: {}", geneSymbol, e.getMessage());
            return 0;
        }
    }
}
