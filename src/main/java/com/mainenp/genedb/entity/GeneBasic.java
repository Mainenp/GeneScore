package com.mainenp.genedb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Gene Basic Information Entity
 * Stores core gene information from custom gene list and HGNC database
 */
@Entity
@Table(name = "gene_basic", indexes = {
    @Index(name = "idx_gene_symbol", columnList = "gene_symbol"),
    @Index(name = "idx_hgnc_id", columnList = "hgnc_id"),
    @Index(name = "idx_entrez_id", columnList = "entrez_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneBasic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Gene symbol (unique identifier from custom gene list)
     */
    @Column(name = "gene_symbol", nullable = false, unique = true, length = 50)
    private String geneSymbol;
    
    /**
     * HGNC ID from official HGNC database
     */
    @Column(name = "hgnc_id", length = 50)
    private String hgncId;
    
    /**
     * Entrez Gene ID from NCBI
     */
    @Column(name = "entrez_id", length = 50)
    private String entrezId;
    
    /**
     * Ensembl Gene ID
     */
    @Column(name = "ensembl_id", length = 50)
    private String ensemblId;
    
    /**
     * Full gene name from HGNC
     */
    @Column(name = "gene_full_name", length = 500)
    private String geneFullName;
    
    /**
     * Gene type (protein-coding, lncRNA, etc.)
     */
    @Column(name = "gene_type", length = 100)
    private String geneType;
    
    /**
     * Gene function description
     */
    @Column(name = "gene_function", columnDefinition = "TEXT")
    private String geneFunction;
    
    /**
     * Record creation time
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * Record update time
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
