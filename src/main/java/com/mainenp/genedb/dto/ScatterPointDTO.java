package com.mainenp.genedb.dto;

public class ScatterPointDTO {
    private Double expressionLevel;
    private Double dependencyScore;
    private String lineage;

    public Double getExpressionLevel() {
        return expressionLevel;
    }

    public void setExpressionLevel(Double expressionLevel) {
        this.expressionLevel = expressionLevel;
    }

    public Double getDependencyScore() {
        return dependencyScore;
    }

    public void setDependencyScore(Double dependencyScore) {
        this.dependencyScore = dependencyScore;
    }

    public String getLineage() {
        return lineage;
    }

    public void setLineage(String lineage) {
        this.lineage = lineage;
    }
}