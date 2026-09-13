package com.mainenp.genedb.repository;

import com.mainenp.genedb.entity.Gene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneRepository extends JpaRepository<Gene, Long> {
    Optional<Gene> findBySymbol(String symbol);
    
    @Query("SELECT g FROM Gene g WHERE LOWER(g.symbol) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Gene> searchBySymbolOrName(@Param("query") String query);
    
    List<Gene> findBySymbolIn(List<String> symbols);
    
    // 新增查询方法
    boolean existsBySymbol(String symbol);
    
    @Query("SELECT COUNT(g) FROM Gene g WHERE g.description IS NOT NULL")
    long countByDescriptionNotNull();
    
    @Query("SELECT COUNT(g) FROM Gene g WHERE g.uniprotId IS NOT NULL")
    long countByUniprotIdNotNull();
    
    @Query("SELECT COUNT(g) FROM Gene g WHERE g.pdbId IS NOT NULL")
    long countByPdbIdNotNull();
    
    @Query("SELECT COUNT(g) FROM Gene g WHERE g.chromosome IS NOT NULL")
    long countByChromosomeNotNull();
}
