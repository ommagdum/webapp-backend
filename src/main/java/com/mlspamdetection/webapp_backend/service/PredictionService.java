package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.PredictionHistoryResponse;
import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.PredictionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PredictionService {

    private final PredictionLogRepository predictionLogRepository;

    @Autowired
    public PredictionService(PredictionLogRepository predictionLogRepository) {
        this.predictionLogRepository = predictionLogRepository;
    }

    public PredictionHistoryResponse getPredictionHistory(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<PredictionHistoryResponse.PredictionHistoryItem> predictionPage =
                predictionLogRepository.findHistoryByUser(user, pageable);

        return new PredictionHistoryResponse(
                predictionPage.getContent(),
                predictionPage.getNumber(),
                predictionPage.getTotalPages()
        );
    }

    public Optional<PredictionLog> getPredictionLogByPredictionIdAndUserId(
        Long predictionId, Long userId) {
        return predictionLogRepository.findByIdAndUserId(predictionId, userId);
    }
    public PredictionLog getPredictionById(Long id, User user) {
        return predictionLogRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Prediction not found with id: " + id));    }

}