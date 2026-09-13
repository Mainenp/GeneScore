package com.mainenp.genedb.repository;

import com.mainenp.genedb.entity.GeneScoreDb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneScoreDbRepository extends JpaRepository<GeneScoreDb, Long> {

    Optional<GeneScoreDb> findByGeneSymbolIgnoreCase(String geneSymbol);

    boolean existsByGeneSymbolIgnoreCase(String geneSymbol);
}
