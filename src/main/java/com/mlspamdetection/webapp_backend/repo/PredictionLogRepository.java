package com.mlspamdetection.webapp_backend.repo;

import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredictionLogRepository extends JpaRepository<PredictionLog, Long> {
    List<PredictionLog> findByUser(User user);
}
