package org.example.expert.config;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AdminLoggingAspect {
    private final ObjectMapper objectMapper;
    private final Set<Class<?>> requestBodyClazz = new HashSet<>();

    public AdminLoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        requestBodyClazz.add(Map.class);
        requestBodyClazz.add(UserRoleChangeRequest.class);
    }

    @Around("@annotation(org.example.expert.config.AdminLogging)")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();

        // 요청 시각, 요청 URL, 요청 ID, 요청 본문, 응답 본문
        LocalDateTime currentTime = LocalDateTime.now();
        String requestURI = request.getRequestURI();
        String requestedUserEmail = getRequestUserEmail(request);

        log.info("= LOG STARTED =");
        log.info("CURRENT TIME: {}", currentTime);
        log.info("REQUEST URI: {}", requestURI);
        log.info("REQUESTED EMAIL: {}", requestedUserEmail);

        Object[] args = joinPoint.getArgs();

        logRequestBody(args);

        Object result = null;
        try {
            result = joinPoint.proceed();
            if (result != null) {
                log.info("RESPONSE BODY: {}", jsonOrNull(result));
            }
        } catch (Exception e) {
            log.error("ERROR OCCURRED: [{}] {}", e.getClass(), e.getMessage());
            throw e;
        } finally {
            log.info("= LOG FINISHED =");
        }
        return result;
    }

    private void logRequestBody(Object[] args) {
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (requestBodyClazz.contains(arg.getClass())) {
                log.info("REQUEST BODY: {}", jsonOrNull(arg));
                break;
            }
        }
    }

    private String getRequestUserEmail(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute("email")).map(Object::toString)
                .orElse("Anon");
    }

    private String jsonOrNull(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("ERROR OCCURRED: {}", e.getMessage());
            return null;
        }
    }
}
