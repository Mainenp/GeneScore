package com.mainenp.genedb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "survival_data", indexes = {
    @Index(name = "idx_gene_lineage_survival", columnList = "gene_id,lineage")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurvivalData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gene_id", nullable = false)
    private Gene gene;
    
    @Column(length = 100)
    private String lineage;
    
    @Column(name = "hazard_ratio")
    private Double hazardRatio;
    
    @Column(name = "p_value")
    private Double pValue;
}