package com.mainenp.genedb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gene_pathways", indexes = {
    @Index(name = "idx_gene_pathway", columnList = "gene_id,pathway_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenePathway {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gene_id", nullable = false)
    private Gene gene;
    
    @Column(name = "pathway_name", nullable = false, length = 255)
    private String pathwayName;
    
    @Column(name = "pathway_id", length = 100)
    private String pathwayId;
    
    @Column(length = 100)
    private String source;
    
    @Column(name = "p_value")
    private Double pValue;
}