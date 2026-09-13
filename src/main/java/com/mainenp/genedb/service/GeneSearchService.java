package com.mainenp.genedb.service;

import com.mainenp.genedb.entity.GeneBasic;
import com.mainenp.genedb.repository.GeneBasicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gene Search Service
 * Provides fuzzy search functionality for genes
 * Searches by gene symbol and full name
 */
@Slf4j
@Service
public class GeneSearchService {
    
    @Autowired
    private GeneBasicRepository geneBasicRepository;
    
    /**
     * Fuzzy search genes by keyword
     * Matches against gene symbol and full name
     * @param keyword search keyword
     * @return List of matching genes with basic information
     */
    public List<Map<String, Object>> searchGenes(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("Empty search keyword provided");
                return List.of();
            }
            
            String searchKeyword = keyword.trim();
            log.info("Searching genes with keyword: {}", searchKeyword);
            
            List<GeneBasic> results = geneBasicRepository.searchByKeyword(searchKeyword);
            
            log.info("Found {} genes matching keyword: {}", results.size(), searchKeyword);
            
            return results.stream()
                    .map(this::convertToSearchResult)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error searching genes with keyword {}: {}", keyword, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Convert GeneBasic entity to search result map
     * @param gene the gene entity
     * @return Map containing basic gene information
     */
    private Map<String, Object> convertToSearchResult(GeneBasic gene) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", gene.getId());
        result.put("geneSymbol", gene.getGeneSymbol());
        result.put("geneFullName", gene.getGeneFullName());
        result.put("hgncId", gene.getHgncId());
        result.put("entrezId", gene.getEntrezId());
        result.put("geneType", gene.getGeneType());
        return result;
    }
    
    /**
     * Get total number of genes in database
     * @return total count
     */
    public long getTotalGeneCount() {
        try {
            long count = geneBasicRepository.count();
            log.info("Total genes in database: {}", count);
            return count;
        } catch (Exception e) {
            log.error("Error getting gene count: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Check if gene exists by symbol
     * @param geneSymbol the gene symbol
     * @return true if exists, false otherwise
     */
    public boolean geneExists(String geneSymbol) {
        try {
            return geneBasicRepository.existsByGeneSymbol(geneSymbol);
        } catch (Exception e) {
            log.error("Error checking gene existence for {}: {}", geneSymbol, e.getMessage());
            return false;
        }
    }
}
