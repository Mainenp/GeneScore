package com.mainenp.genedb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "gene_scores", indexes = {
    @Index(name = "idx_gene_lineage", columnList = "gene_id,lineage"),
    @Index(name = "idx_lineage", columnList = "lineage"),
    @Index(name = "idx_cancer_type", columnList = "cancer_type"),
    @Index(name = "idx_tissue_type", columnList = "tissue_type"),
    @Index(name = "idx_dataset", columnList = "dataset")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gene_id", nullable = false)
    private Gene gene;
    
    @Column(name = "lineage", length = 100)
    private String lineage;
    
    @Column(name = "disease_subtype", length = 100)
    private String diseaseSubtype;
    
    @Column(name = "dependency_score")
    private Double dependencyScore;
    
    @Column(name = "expression_level")
    private Double expressionLevel;
    
    @Column(name = "copy_number")
    private Double copyNumber;
    
    @Column(length = 50)
    private String dataset;
    
    @Column(name = "replicate_count")
    private Integer replicateCount;
    
    // 新增字段：用户模型预测分数
    @Column(name = "model_prediction_score")
    private Double modelPredictionScore;
    
    @Column(name = "cancer_type", length = 100)
    private String cancerType;
    
    @Column(name = "tissue_type", length = 100)
    private String tissueType;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "prediction_date")
    private LocalDate predictionDate;
}