package com.mainenp.genedb.repository;

import com.mainenp.genedb.entity.SurvivalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurvivalDataRepository extends JpaRepository<SurvivalData, Long> {
    List<SurvivalData> findByGeneSymbol(String symbol);
    
    List<SurvivalData> findByGeneSymbolAndLineage(String symbol, String lineage);
}
