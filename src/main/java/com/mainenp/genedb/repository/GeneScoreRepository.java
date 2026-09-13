package com.mainenp.genedb.repository;

import com.mainenp.genedb.entity.GeneScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneScoreRepository extends JpaRepository<GeneScore, Long> {
    List<GeneScore> findByGeneSymbol(String symbol);
    
    @Query("SELECT gs FROM GeneScore gs WHERE gs.gene.symbol = :symbol GROUP BY gs.lineage")
    List<GeneScore> findByGeneSymbolGroupedByLineage(@Param("symbol") String symbol);
    
    @Query("SELECT gs FROM GeneScore gs WHERE gs.gene.symbol = :symbol AND gs.lineage = :lineage")
    List<GeneScore> findByGeneSymbolAndLineage(@Param("symbol") String symbol, @Param("lineage") String lineage);
    
    // 新增查询方法
    List<GeneScore> findByDataset(String dataset);
    
    List<GeneScore> findByCancerType(String cancerType);
    
    List<GeneScore> findByTissueType(String tissueType);
    
    @Query("SELECT gs FROM GeneScore gs WHERE gs.gene.symbol = :symbol AND gs.dataset = :dataset")
    List<GeneScore> findByGeneSymbolAndDataset(@Param("symbol") String symbol, @Param("dataset") String dataset);
    
    @Query("SELECT gs FROM GeneScore gs WHERE gs.gene.symbol = :symbol AND gs.cancerType = :cancerType")
    List<GeneScore> findByGeneSymbolAndCancerType(@Param("symbol") String symbol, @Param("cancerType") String cancerType);
    
    @Query("SELECT COUNT(gs) FROM GeneScore gs WHERE gs.dataset = :dataset")
    long countByDataset(@Param("dataset") String dataset);
    
    @Query("SELECT gs.dataset, COUNT(gs) FROM GeneScore gs GROUP BY gs.dataset")
    List<Object[]> countByDataset();
    
    @Query("SELECT DISTINCT gs.cancerType FROM GeneScore gs WHERE gs.cancerType IS NOT NULL")
    List<String> findDistinctCancerTypes();
    
    @Query("SELECT DISTINCT gs.tissueType FROM GeneScore gs WHERE gs.tissueType IS NOT NULL")
    List<String> findDistinctTissueTypes();
}
