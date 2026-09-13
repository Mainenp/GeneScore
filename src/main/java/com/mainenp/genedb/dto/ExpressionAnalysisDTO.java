package com.mainenp.genedb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionAnalysisDTO {
    private Double log2FC;
    private Double p_value;
    private Double tumor_mean;
    private Double normal_mean;
    private Integer tumor_count;
    private Integer normal_count;
    private String status;
}
