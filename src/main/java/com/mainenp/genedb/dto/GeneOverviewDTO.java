package com.mainenp.genedb.dto;

public class GeneOverviewDTO {
    private Long id;
    private String symbol;
    private String name;
    private String entrezId;
    private String uniprotId;
    private String description;
    private String biologicalProcess;
    private String molecularFunction;
    private String pdbId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntrezId() {
        return entrezId;
    }

    public void setEntrezId(String entrezId) {
        this.entrezId = entrezId;
    }

    public String getUniprotId() {
        return uniprotId;
    }

    public void setUniprotId(String uniprotId) {
        this.uniprotId = uniprotId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBiologicalProcess() {
        return biologicalProcess;
    }

    public void setBiologicalProcess(String biologicalProcess) {
        this.biologicalProcess = biologicalProcess;
    }

    public String getMolecularFunction() {
        return molecularFunction;
    }

    public void setMolecularFunction(String molecularFunction) {
        this.molecularFunction = molecularFunction;
    }

    public String getPdbId() {
        return pdbId;
    }

    public void setPdbId(String pdbId) {
        this.pdbId = pdbId;
    }
}