package com.mainenp.genedb.repository;

import com.mainenp.genedb.entity.GenePathway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenePathwayRepository extends JpaRepository<GenePathway, Long> {
    List<GenePathway> findByGeneSymbol(String symbol);
}
