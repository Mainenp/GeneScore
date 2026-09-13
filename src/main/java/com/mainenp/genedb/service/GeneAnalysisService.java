package com.mainenp.genedb.service;

import com.mainenp.genedb.dto.GeneAnalysisResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GeneAnalysisService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${python.microservice.url:http://localhost:8000}")
    private String pythonMicroserviceUrl;

    /**
     * Get comprehensive gene analysis data from Python microservice
     * @param geneSymbol gene symbol
     * @param cancerType cancer type (default: LUAD)
     * @return GeneAnalysisResponseDTO with analysis data
     */
    public GeneAnalysisResponseDTO getGeneAnalysis(String geneSymbol, String cancerType) {
        String finalCancerType = (cancerType != null && !cancerType.isEmpty()) ? cancerType : "LUAD";

        try {
            // Call Python microservice API: /api/analysis/{gene_symbol}?cancer_type={cancer_type}
            String url = String.format("%s/api/analysis/%s?cancer_type=%s",
                    pythonMicroserviceUrl,
                    geneSymbol.toUpperCase(),
                    finalCancerType.toUpperCase());

            log.info("Calling Python microservice at: {}", url);

            ResponseEntity<GeneAnalysisResponseDTO> response = restTemplate.getForEntity(url, GeneAnalysisResponseDTO.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully retrieved analysis data from Python microservice for gene: {}", geneSymbol);
                GeneAnalysisResponseDTO rawData = response.getBody();
                
                // Normalize to consistent camelCase format for frontend
                GeneAnalysisResponseDTO normalizedData = new GeneAnalysisResponseDTO();
                normalizedData.setGeneSymbol(rawData.getEffectiveGeneSymbol());
                normalizedData.setCancerType(rawData.getEffectiveCancerType());
                normalizedData.setExpression(rawData.getExpression());
                normalizedData.setPrognosis(rawData.getPrognosis());
                normalizedData.setDrug(rawData.getEffectiveDrugSensitivity());
                
                return normalizedData;
            } else {
                log.warn("Python microservice returned non-success status: {}", response.getStatusCode());
                return null;
            }

        } catch (ResourceAccessException e) {
            log.error("Python microservice not accessible: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error retrieving gene analysis from Python microservice for {}: {}", geneSymbol, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get gene analysis data with default cancer type
     */
    public GeneAnalysisResponseDTO getGeneAnalysis(String geneSymbol) {
        return getGeneAnalysis(geneSymbol, "LUAD");
    }
}
