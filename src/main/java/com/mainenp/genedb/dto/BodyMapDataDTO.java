package com.mainenp.genedb.dto;

public class BodyMapDataDTO {
    private String lineage;
    private Double averageDependencyScore;

    public String getLineage() {
        return lineage;
    }

    public void setLineage(String lineage) {
        this.lineage = lineage;
    }

    public Double getAverageDependencyScore() {
        return averageDependencyScore;
    }

    public void setAverageDependencyScore(Double averageDependencyScore) {
        this.averageDependencyScore = averageDependencyScore;
    }
}