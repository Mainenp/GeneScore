package com.mainenp.genedb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "genes", indexes = {
    @Index(name = "idx_symbol", columnList = "symbol", unique = true),
    @Index(name = "idx_uniprot", columnList = "uniprot_id"),
    @Index(name = "idx_chromosome", columnList = "chromosome")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String symbol;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(name = "entrez_id", length = 50)
    private String entrezId;
    
    @Column(name = "uniprot_id", length = 50)
    private String uniprotId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "chromosome", length = 5)
    private String chromosome;
    
    @Column(name = "start_position")
    private Long startPosition;
    
    @Column(name = "end_position")
    private Long endPosition;
    
    @Column(name = "strand", length = 1)
    private String strand;
    
    @Column(name = "biotype", length = 50)
    private String biotype;
    
    @Column(name = "go_biological_process", columnDefinition = "TEXT")
    private String goBiologicalProcess;
    
    @Column(name = "go_molecular_function", columnDefinition = "TEXT")
    private String goMolecularFunction;
    
    @Column(name = "go_cellular_component", columnDefinition = "TEXT")
    private String goCellularComponent;
    
    @Column(name = "protein_length")
    private Integer proteinLength;
    
    @Column(name = "pdb_id", length = 10)
    private String pdbId;
    
    @OneToMany(mappedBy = "gene", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GeneScore> geneScores;
    
    @OneToMany(mappedBy = "gene", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GeneDrug> geneDrugs;
    
    @OneToMany(mappedBy = "gene", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GenePathway> genePathways;
}