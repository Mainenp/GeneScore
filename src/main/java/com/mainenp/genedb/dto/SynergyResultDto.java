package com.mainenp.genedb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SynergyResultDto {
    private String gene_A;
    private String gene_B;
    private double predicted_synergy;
}
