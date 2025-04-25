package com.mlspamdetection.webapp_backend.repo;

import com.mlspamdetection.webapp_backend.dto.PredictionHistoryResponse;
import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionLogRepository extends JpaRepository<PredictionLog, Long> {
    Page<PredictionLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);

    Optional<PredictionLog> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    long countByUserAndIsSpam(User user, boolean isSpam);

    @Query("SELECT COUNT(p) FROM PredictionLog p WHERE p.user = ?1 AND p.timestamp >= ?2 AND p.timestamp < ?3")
    long countByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT CAST(p.timestamp AS DATE) as date, COUNT(p) as count FROM PredictionLog p WHERE p.user = ?1 AND p.timestamp >= ?2 GROUP BY CAST(p.timestamp AS DATE) ORDER BY date")
    List<Object[]> getDailyCounts(User user, LocalDateTime startDate);

    @Query("SELECT new com.mlspamdetection.webapp_backend.dto.PredictionHistoryResponse$PredictionHistoryItem(" +
            "p.id, SUBSTRING(p.content, 1, 50), p.isSpam, p.confidence, p.timestamp) " +
            "FROM PredictionLog p WHERE p.user = :user")
    Page<PredictionHistoryResponse.PredictionHistoryItem> findHistoryByUser(User user, Pageable pageable);

    Optional<PredictionLog> findByIdAndUserId(Long predictionId, Long userId);
}
