package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.PredictionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
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

            Map<String, String> body = new HashMap<>();
            body.put("email_text", emailText);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            return restTemplate.postForObject(
                    mlServiceUrl + "/predict",
                    request,
                    PredictionResult.class
            );

        } catch (Exception e) {
            log.error("ML service error", e);
            throw new RuntimeException("Failed to get prediction: " + e.getMessage());
        }
    }
}
