package com.mainenp.genedb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gene_drugs", indexes = {
    @Index(name = "idx_gene_drug", columnList = "gene_id,drug_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneDrug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gene_id", nullable = false)
    private Gene gene;
    
    @Column(name = "drug_name", nullable = false, length = 255)
    private String drugName;
    
    @Column(name = "drug_id", length = 100)
    private String drugId;
    
    @Column(name = "interaction_type", length = 100)
    private String interactionType;
    
    @Column(name = "evidence_type", length = 100)
    private String evidenceType;
    
    @Column(name = "evidence_count")
    private Integer evidenceCount;
    
    @Column(length = 100)
    private String source;
}