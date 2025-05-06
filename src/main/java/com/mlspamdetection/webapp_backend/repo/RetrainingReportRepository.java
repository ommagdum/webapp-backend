package com.mlspamdetection.webapp_backend.repo;

import com.mlspamdetection.webapp_backend.model.RetrainingReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RetrainingReportRepository extends JpaRepository<RetrainingReport, Long> {

    List<RetrainingReport> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    RetrainingReport findTopByOrderByTimestampDesc();

    Optional<RetrainingReport> findByIsActiveTrue();

    Optional<RetrainingReport> findTopBySuccessTrueOrderByTimestampDesc();

    @Modifying
    @Transactional
    @Query("UPDATE RetrainingReport r SET r.isActive = false")
    void deactivateAllModels();

    Optional<RetrainingReport> findByModelVersion(String modelVersion);
}
