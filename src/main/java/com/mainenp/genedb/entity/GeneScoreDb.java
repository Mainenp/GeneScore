package com.mainenp.genedb.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * GeneScoreDb 实体 - 专门用于存储 gene_score_db.csv 数据
 */
@Entity
@Table(name = "gene_score_db", indexes = {
    @Index(name = "idx_gene_symbol", columnList = "gene_symbol")
})
@Data
public class GeneScoreDb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gene_symbol", nullable = false, length = 50)
    private String geneSymbol;

    @Column(name = "bowel")
    private Double bowel;

    @Column(name = "breast")
    private Double breast;

    @Column(name = "brain")
    private Double brain;

    @Column(name = "stomach")
    private Double stomach;

    @Column(name = "head_and_neck")
    private Double headAndNeck;

    @Column(name = "kidney")
    private Double kidney;

    @Column(name = "liver")
    private Double liver;

    @Column(name = "lung")
    private Double lung;

    @Column(name = "ovary")
    private Double ovary;

    @Column(name = "pancreas")
    private Double pancreas;

    @Column(name = "prostate")
    private Double prostate;

    @Column(name = "skin")
    private Double skin;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 根据组织名称获取分数
     */
    public Double getScoreByTissue(String tissue) {
        return switch (tissue.toLowerCase().replace(" ", "_")) {
            case "bowel" -> bowel;
            case "breast" -> breast;
            case "brain" -> brain;
            case "stomach" -> stomach;
            case "head_and_neck", "headandneck" -> headAndNeck;
            case "kidney" -> kidney;
            case "liver" -> liver;
            case "lung" -> lung;
            case "ovary" -> ovary;
            case "pancreas" -> pancreas;
            case "prostate" -> prostate;
            case "skin" -> skin;
            default -> null;
        };
    }
}
