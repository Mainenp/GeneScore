package com.mainenp.genedb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "gene_cancer_scores", indexes = {
    @Index(name = "idx_gene_cancer", columnList = "gene_id,cancer_type"),
    @Index(name = "idx_cancer_type", columnList = "cancer_type"),
    @Index(name = "idx_tissue_type", columnList = "tissue_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneCancerScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gene_id", nullable = false)
    private Gene gene;
    
    @Column(name = "cancer_type", length = 100, nullable = false)
    private String cancerType;
    
    @Column(name = "tissue_type", length = 100)
    private String tissueType;
    
    @Column(name = "score", nullable = false)
    private Double score;
    
    @Column(name = "ranking")
    private Integer ranking;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "dataset", length = 50)
    private String dataset;
    
    @Column(name = "prediction_date")
    private LocalDate predictionDate;
    
    @Column(name = "created_at")
    private LocalDate createdAt;
}
