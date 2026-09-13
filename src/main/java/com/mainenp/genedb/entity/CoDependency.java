package com.mainenp.genedb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "co_dependencies", indexes = {
    @Index(name = "idx_gene_pair", columnList = "gene_id_a,gene_id_b")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoDependency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gene_id_a", nullable = false)
    private Gene geneA;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gene_id_b", nullable = false)
    private Gene geneB;
    
    @Column(name = "pearson_correlation")
    private Double pearsonCorrelation;
    
    @Column(name = "p_value")
    private Double pValue;
    
    @Column(name = "sample_count")
    private Integer sampleCount;
    
    @Column(length = 50)
    private String dataset;
}