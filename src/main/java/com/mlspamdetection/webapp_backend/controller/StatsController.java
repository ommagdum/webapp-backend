package com.mlspamdetection.webapp_backend.controller;

import com.mlspamdetection.webapp_backend.dto.StatsResponse;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<StatsResponse> getUserStats(@AuthenticationPrincipal User user) {
        if (user == null) {
            System.out.println("Unauthenticated stats access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new StatsResponse());
        }

        System.out.println("Getting stats for user: " + user.getEmail() + " (ID: " + user.getId() + ")");

        StatsResponse stats = statsService.getUserStats(user);

        if (stats == null) {
            System.out.println("Null stats generated for user ID: " + user.getId());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(stats);
    }

}
