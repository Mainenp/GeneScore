package com.mainenp.genedb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugSensitivityDTO {
    private String drug_name;
    private String target_pathway;
    private String target_genes;
    private Double mean_ic50;
    private Integer tested_cell_lines;
}
