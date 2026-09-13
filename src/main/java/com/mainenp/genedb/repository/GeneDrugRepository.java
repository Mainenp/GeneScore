package com.mainenp.genedb.repository;

import com.mainenp.genedb.entity.GeneDrug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneDrugRepository extends JpaRepository<GeneDrug, Long> {
    List<GeneDrug> findByGeneSymbol(String symbol);
}
