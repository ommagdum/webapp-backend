package com.mlspamdetection.webapp_backend.repo;

import com.mlspamdetection.webapp_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findByVerificationToken(String token);

    boolean existsByEmail(String email);
}
