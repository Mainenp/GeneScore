package com.mainenp.genedb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Gene Cache Entity
 * Caches API call results to avoid repeated requests to public APIs
 * Cache validity: 1 day (86400 seconds)
 */
@Entity
@Table(name = "gene_cache", indexes = {
    @Index(name = "idx_gene_symbol", columnList = "gene_symbol"),
    @Index(name = "idx_expire_time", columnList = "expire_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Gene symbol (foreign key reference to gene_basic)
     */
    @Column(name = "gene_symbol", nullable = false, length = 50)
    private String geneSymbol;
    
    /**
     * Data type enum: 3D_STRUCTURE, OMICS, NETWORK, DRUG, PATHWAY
     */
    @Column(name = "data_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DataType dataType;
    
    /**
     * Cached API response data in JSON format
     */
    @Column(name = "data_content", nullable = false, columnDefinition = "LONGTEXT")
    private String dataContent;
    
    /**
     * Cache creation time
     */
    @Column(name = "cache_time", nullable = false, updatable = false)
    private LocalDateTime cacheTime;
    
    /**
     * Cache expiration time (1 day from cache_time)
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    /**
     * Data type enum for different API sources
     */
    public enum DataType {
        /**
         * 3D protein structure from AlphaFold DB
         */
        STRUCTURE_3D("3D_STRUCTURE"),
        
        /**
         * Multi-omics data from GTEx
         */
        OMICS("OMICS"),
        
        /**
         * Gene interaction network from STRING
         */
        NETWORK("NETWORK"),
        
        /**
         * Drug targeting information from DGIdb
         */
        DRUG("DRUG"),
        
        /**
         * Gene pathway information from KEGG/Reactome
         */
        PATHWAY("PATHWAY");
        
        private final String value;
        
        DataType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        cacheTime = LocalDateTime.now();
        // Set expiration time to 1 day from now
        expireTime = cacheTime.plusDays(1);
    }
}
