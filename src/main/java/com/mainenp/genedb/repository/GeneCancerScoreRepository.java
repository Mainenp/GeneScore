package com.mainenp.genedb.repository;

import com.mainenp.genedb.entity.GeneCancerScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneCancerScoreRepository extends JpaRepository<GeneCancerScore, Long> {
    
    /**
     * 根据基因符号和癌症类型查询分数
     */
    @Query("SELECT gcs FROM GeneCancerScore gcs WHERE gcs.gene.symbol = :symbol AND gcs.cancerType = :cancerType")
    List<GeneCancerScore> findByGeneSymbolAndCancerType(@Param("symbol") String symbol, @Param("cancerType") String cancerType);
    
    /**
     * 根据基因符号查询所有癌症的分数
     */
    @Query("SELECT gcs FROM GeneCancerScore gcs WHERE gcs.gene.symbol = :symbol ORDER BY gcs.cancerType")
    List<GeneCancerScore> findByGeneSymbol(@Param("symbol") String symbol);
    
    /**
     * 根据癌症类型查询所有基因的分数
     */
    List<GeneCancerScore> findByCancerType(String cancerType);
    
    /**
     * 根据组织类型查询分数
     */
    List<GeneCancerScore> findByTissueType(String tissueType);
    
    /**
     * 查询特定基因在特定癌症中的分数（用于人体地图）
     */
    @Query("SELECT gcs FROM GeneCancerScore gcs WHERE gcs.gene.symbol = :symbol AND gcs.cancerType = :cancerType LIMIT 1")
    GeneCancerScore findScoreForBodyMap(@Param("symbol") String symbol, @Param("cancerType") String cancerType);
    
    /**
     * 获取所有不同的癌症类型
     */
    @Query("SELECT DISTINCT gcs.cancerType FROM GeneCancerScore gcs ORDER BY gcs.cancerType")
    List<String> findDistinctCancerTypes();
    
    /**
     * 获取所有不同的组织类型
     */
    @Query("SELECT DISTINCT gcs.tissueType FROM GeneCancerScore gcs WHERE gcs.tissueType IS NOT NULL ORDER BY gcs.tissueType")
    List<String> findDistinctTissueTypes();
    
    /**
     * 统计特定癌症类型的分数记录数
     */
    long countByCancerType(String cancerType);
    
    /**
     * 获取特定基因在所有癌症中的分数统计
     */
    @Query("SELECT gcs.cancerType, AVG(gcs.score) as avgScore FROM GeneCancerScore gcs WHERE gcs.gene.symbol = :symbol GROUP BY gcs.cancerType")
    List<Object[]> getAverageScoresByGeneAndCancerType(@Param("symbol") String symbol);
}
