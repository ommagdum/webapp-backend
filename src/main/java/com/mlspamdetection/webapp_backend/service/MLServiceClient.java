package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.PredictionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class MLServiceClient {
    @Value("${ml.service.url}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate;

    public MLServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public PredictionResult getPrediction(String emailText) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> request = new HashMap<>();
            request.put("email_text", emailText);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    mlServiceUrl,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                if (body == null || !"success".equals(body.get("status"))) {
                    throw new RuntimeException("ML Service error: " + body.get("error"));
                }

                return new PredictionResult(
                        (String) body.get("prediction"),  // "spam" or "ham"
                        ((Number) body.get("probability")).doubleValue()
                );
            }
            throw new RuntimeException("ML Service responded with status: " + response.getStatusCode());
        } catch (RestClientException e) {
            throw new RuntimeException("ML Service communication failed: " + e.getMessage());
        }
    }
}