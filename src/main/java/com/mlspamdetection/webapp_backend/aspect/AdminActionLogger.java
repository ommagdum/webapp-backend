package com.mlspamdetection.webapp_backend.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminActionLogger {

    private static final Logger logger = LoggerFactory.getLogger(AdminActionLogger.class);

    @AfterReturning("execution(* com.mlspamdetection.webapp_backend.controller.AdminController.*(..))")
    public void logAdminAction(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = authentication != null ? authentication.getName() : "unknown";

        logger.info("ADMIN ACTION: User '{}' executed '{}' with arguments: {}",
                adminEmail,
                joinPoint.getSignature().getName(),
                joinPoint.getArgs());
    }
}
