package com.mlspamdetection.webapp_backend.controller;

import com.mlspamdetection.webapp_backend.model.RetrainingReport;
import com.mlspamdetection.webapp_backend.service.RetrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/retraining")
public class RetrainingController {

    private final RetrainingService retrainingService;

    @Autowired
    public RetrainingController(RetrainingService retrainingService){
        this.retrainingService = retrainingService;
    }

    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RetrainingReport> triggerRetraining() {
        RetrainingReport report = retrainingService.processUnprocessedFeedback();
        return ResponseEntity.ok(report);
    }
}
