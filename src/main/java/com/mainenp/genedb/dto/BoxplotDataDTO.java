package com.mainenp.genedb.dto;

import java.util.List;

public class BoxplotDataDTO {
    private String lineage;
    private List<Double> scores;

    public String getLineage() {
        return lineage;
    }

    public void setLineage(String lineage) {
        this.lineage = lineage;
    }

    public List<Double> getScores() {
        return scores;
    }

    public void setScores(List<Double> scores) {
        this.scores = scores;
    }
}