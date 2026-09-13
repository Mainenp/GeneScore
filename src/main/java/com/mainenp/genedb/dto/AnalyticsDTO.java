package com.mainenp.genedb.dto;

import java.util.List;

public class AnalyticsDTO {
    private List<ScatterPointDTO> scatterData;
    private List<BoxplotDataDTO> boxplotData;

    public List<ScatterPointDTO> getScatterData() {
        return scatterData;
    }

    public void setScatterData(List<ScatterPointDTO> scatterData) {
        this.scatterData = scatterData;
    }

    public List<BoxplotDataDTO> getBoxplotData() {
        return boxplotData;
    }

    public void setBoxplotData(List<BoxplotDataDTO> boxplotData) {
        this.boxplotData = boxplotData;
    }
}