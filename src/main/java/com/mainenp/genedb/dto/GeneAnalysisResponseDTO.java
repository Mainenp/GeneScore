package com.mainenp.genedb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneAnalysisResponseDTO {
    private String geneSymbol;
    private String gene_symbol;
    private String cancerType;
    private String cancer_type;
    private ExpressionAnalysisDTO expression;
    private PrognosisAnalysisDTO prognosis;
    private List<DrugSensitivityDTO> drug;
    private List<DrugSensitivityDTO> drug_sensitivity;
    
    // Helper getters for consistency
    public String getEffectiveGeneSymbol() {
        return geneSymbol != null ? geneSymbol : gene_symbol;
    }
    
    public String getEffectiveCancerType() {
        return cancerType != null ? cancerType : cancer_type;
    }
    
    public List<DrugSensitivityDTO> getEffectiveDrugSensitivity() {
        return drug != null && !drug.isEmpty() ? drug : drug_sensitivity;
    }
}
