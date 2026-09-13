package com.mainenp.genedb.repository;

import com.mainenp.genedb.entity.GeneCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for GeneCache entity
 * Provides database access methods for cached API results
 */
@Repository
public interface GeneCacheRepository extends JpaRepository<GeneCache, Long> {
    
    /**
     * Find cached data by gene symbol and data type
     * @param geneSymbol the gene symbol
     * @param dataType the data type
     * @return Optional containing cached data if found and not expired
     */
    @Query("SELECT gc FROM GeneCache gc WHERE gc.geneSymbol = :geneSymbol " +
           "AND gc.dataType = :dataType AND gc.expireTime > :now ORDER BY gc.cacheTime DESC")
    List<GeneCache> findValidCaches(@Param("geneSymbol") String geneSymbol,
                                    @Param("dataType") GeneCache.DataType dataType,
                                    @Param("now") LocalDateTime now);
    
    /**
     * Find latest valid cached data by gene symbol and data type
     * @param geneSymbol the gene symbol
     * @param dataType the data type
     * @return Optional containing cached data if found and not expired
     */
    default Optional<GeneCache> findValidCache(String geneSymbol, GeneCache.DataType dataType, LocalDateTime now) {
        List<GeneCache> caches = findValidCaches(geneSymbol, dataType, now);
        return caches.isEmpty() ? Optional.empty() : Optional.of(caches.get(0));
    }
    
    /**
     * Find all cached data for a gene
     * @param geneSymbol the gene symbol
     * @return list of cached data for the gene
     */
    List<GeneCache> findByGeneSymbol(String geneSymbol);
    
    /**
     * Find all expired cache entries
     * @param now current time
     * @return list of expired cache entries
     */
    @Query("SELECT gc FROM GeneCache gc WHERE gc.expireTime <= :now")
    List<GeneCache> findExpiredCache(@Param("now") LocalDateTime now);
    
    /**
     * Delete expired cache entries
     * @param now current time
     * @return number of deleted records
     */
    @Query("DELETE FROM GeneCache gc WHERE gc.expireTime <= :now")
    int deleteExpiredCache(@Param("now") LocalDateTime now);
    
    /**
     * Check if valid cache exists for gene and data type
     * @param geneSymbol the gene symbol
     * @param dataType the data type
     * @param now current time
     * @return true if valid cache exists
     */
    @Query("SELECT COUNT(gc) > 0 FROM GeneCache gc WHERE gc.geneSymbol = :geneSymbol " +
           "AND gc.dataType = :dataType AND gc.expireTime > :now")
    boolean existsValidCache(@Param("geneSymbol") String geneSymbol,
                            @Param("dataType") GeneCache.DataType dataType,
                            @Param("now") LocalDateTime now);
}
