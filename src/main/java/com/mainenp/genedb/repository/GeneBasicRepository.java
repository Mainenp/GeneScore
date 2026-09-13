package com.mainenp.genedb.repository;

import com.mainenp.genedb.entity.GeneBasic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GeneBasic entity
 * Provides database access methods for gene basic information
 */
@Repository
public interface GeneBasicRepository extends JpaRepository<GeneBasic, Long> {
    
    /**
     * Find gene by gene symbol
     * @param geneSymbol the gene symbol
     * @return Optional containing the gene if found
     */
    Optional<GeneBasic> findByGeneSymbol(String geneSymbol);
    
    /**
     * Find gene by HGNC ID
     * @param hgncId the HGNC ID
     * @return Optional containing the gene if found
     */
    Optional<GeneBasic> findByHgncId(String hgncId);
    
    /**
     * Find gene by Entrez ID
     * @param entrezId the Entrez ID
     * @return Optional containing the gene if found
     */
    Optional<GeneBasic> findByEntrezId(String entrezId);
    
    /**
     * Fuzzy search genes by symbol or full name
     * @param keyword search keyword
     * @return list of matching genes
     */
    @Query("SELECT g FROM GeneBasic g WHERE " +
           "LOWER(g.geneSymbol) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.geneFullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<GeneBasic> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Get total count of genes in database
     * @return total number of genes
     */
    long count();
    
    /**
     * Check if gene exists by symbol
     * @param geneSymbol the gene symbol
     * @return true if exists, false otherwise
     */
    boolean existsByGeneSymbol(String geneSymbol);
}
