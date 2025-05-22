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

/**
 * Client service for communicating with the ML prediction service.
 * 
 * <p>This service acts as a client to the external machine learning service that
 * performs spam detection. It handles the HTTP communication, request formatting,
 * and response parsing required to obtain spam predictions for email text.</p>
 * 
 * <p>The service uses Spring's RestTemplate to make HTTP requests to the ML service
 * endpoint configured via application properties. It includes error handling and
 * logging to help diagnose issues with the ML service communication.</p>
 */
@Service
@Slf4j
public class MLServiceClient {
    /**
     * URL of the ML service endpoint, injected from application properties.
     */
    @Value("${ml.service.url}")
    private String mlServiceUrl;

    /**
     * RestTemplate instance for making HTTP requests to the ML service.
     */
    private final RestTemplate restTemplate;

    /**
     * Constructs a new MLServiceClient with a default RestTemplate.
     */
    public MLServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sends an email text to the ML service and retrieves a spam prediction.
     * 
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Prepares an HTTP request with the email text as JSON payload</li>
     *   <li>Sends the request to the ML service's prediction endpoint</li>
     *   <li>Processes the response to extract the prediction result</li>
     *   <li>Handles various response formats and error conditions</li>
     * </ol>
     * 
     * <p>The method is designed to be resilient to different response formats from the ML service,
     * handling both numeric (0/1) and string ("ham"/"spam") prediction values, as well as
     * nested response structures.</p>
     *
     * @param emailText the email text to analyze for spam detection
     * @return a PredictionResult containing the classification ("spam" or "ham") and confidence score
     * @throws RuntimeException if communication with the ML service fails or returns an error
     */
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
