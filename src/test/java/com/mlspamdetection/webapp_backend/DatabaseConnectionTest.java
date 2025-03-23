package com.mlspamdetection.webapp_backend;

import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DatabaseConnectionTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void testSaveUser(){
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());
    }
}
