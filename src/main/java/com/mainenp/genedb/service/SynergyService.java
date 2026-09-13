package com.mainenp.genedb.service;

import com.mainenp.genedb.dto.SynergyResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class SynergyService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${python.synergy.service.url:http://localhost:8000}")
    private String synergyServiceUrl;

    public SynergyResponseDto predictSynergy(String genes) {
        try {
            log.info("Calling synergy prediction service for genes: {}", genes);

            String url = UriComponentsBuilder
                    .fromUriString(synergyServiceUrl + "/api/predict_synergy")
                    .queryParam("genes", genes)
                    .build()
                    .toUriString();

            SynergyResponseDto response = restTemplate.getForObject(url, SynergyResponseDto.class);

            log.info("Synergy prediction successful, {} pairs tested", response.getPairs_tested());
            return response;

        } catch (RestClientException e) {
            log.error("Failed to call synergy prediction service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call synergy prediction service", e);
        }
    }
}
