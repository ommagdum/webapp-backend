package com.mlspamdetection.webapp_backend.controller;

import com.mlspamdetection.webapp_backend.dto.PredictionHistoryResponse;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.service.PredictionService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/predictions")
public class PredictionHistoryController {

    private final PredictionService historyService;

    public PredictionHistoryController(PredictionService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public ResponseEntity<PredictionHistoryResponse> getHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(historyService.getPredictionHistory(user, page, size));
    }
}