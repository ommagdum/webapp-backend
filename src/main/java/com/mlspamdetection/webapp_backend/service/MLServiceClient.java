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

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email_text", emailText);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // Log the request for debugging
            System.out.println("Sending request to ML service: " + mlServiceUrl + "/predict");

            // Change the return type to Map to process the response manually
            Map<String, Object> response = restTemplate.postForObject(
                    mlServiceUrl + "/predict",
                    request,
                    Map.class
            );

            // Log the response for debugging
            System.out.println("Received response from ML service: " + response);

            // If response is null
            if (response == null) {
                System.out.println("ML service returned null response");
                return new PredictionResult("unknown", 0.0);
            }

            // Check if the response has a success field and it's true
            if (response.containsKey("success") && Boolean.FALSE.equals(response.get("success"))) {
                String errorMsg = response.containsKey("error") ? (String) response.get("error") : "Unknown error";
                throw new RuntimeException("ML Service error: " + errorMsg);
            }

            // Extract data from the nested structure
            Map<String, Object> data = response.containsKey("data") ?
                    (Map<String, Object>) response.get("data") : response;

            // Extract prediction and convert from integer to string
            String prediction = "unknown";
            if (data.containsKey("prediction")) {
                Object predObj = data.get("prediction");
                if (predObj instanceof Number) {
                    // Convert 0/1 to "ham"/"spam"
                    int predValue = ((Number) predObj).intValue();
                    prediction = (predValue == 1) ? "spam" : "ham";
                } else if (predObj instanceof String) {
                    prediction = (String) predObj;
                }
            }

            // Extract probability
            Double probability = 0.0;
            if (data.containsKey("probability")) {
                Object probObj = data.get("probability");
                if (probObj instanceof Number) {
                    probability = ((Number) probObj).doubleValue();
                } else if (probObj instanceof String) {
                    try {
                        probability = Double.parseDouble((String) probObj);
                    } catch (NumberFormatException e) {
                        System.out.println("Could not parse probability: " + probObj);
                    }
                }
            }

            return new PredictionResult(prediction, probability);

        } catch (Exception e) {
            System.out.println("ML service error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get prediction: " + e.getMessage());
        }
    }

}
