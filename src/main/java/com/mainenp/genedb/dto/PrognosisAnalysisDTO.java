package com.mainenp.genedb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrognosisAnalysisDTO {
    private Double hazard_ratio;
    private Double p_value;
    private String risk;
    private Integer sample_size;
}
