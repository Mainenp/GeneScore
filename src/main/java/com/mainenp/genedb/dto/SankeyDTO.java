package com.mainenp.genedb.dto;

import java.util.List;

public class SankeyDTO {
    private List<SankeyNodeDTO> nodes;
    private List<SankeyLinkDTO> links;

    public List<SankeyNodeDTO> getNodes() {
        return nodes;
    }

    public void setNodes(List<SankeyNodeDTO> nodes) {
        this.nodes = nodes;
    }

    public List<SankeyLinkDTO> getLinks() {
        return links;
    }

    public void setLinks(List<SankeyLinkDTO> links) {
        this.links = links;
    }
}