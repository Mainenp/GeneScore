package com.mainenp.genedb.repository;

import com.mainenp.genedb.entity.CoDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoDependencyRepository extends JpaRepository<CoDependency, Long> {
    @Query("SELECT cd FROM CoDependency cd WHERE cd.geneA.symbol = :symbol OR cd.geneB.symbol = :symbol")
    List<CoDependency> findByGeneSymbol(@Param("symbol") String symbol);
    
    @Query("SELECT cd FROM CoDependency cd WHERE (cd.geneA.symbol = :geneA AND cd.geneB.symbol = :geneB) OR (cd.geneA.symbol = :geneB AND cd.geneB.symbol = :geneA)")
    List<CoDependency> findByGenePair(@Param("geneA") String geneA, @Param("geneB") String geneB);
}
