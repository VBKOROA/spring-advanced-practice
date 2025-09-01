package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void 잘못된_요청_예외_테스트() {
        InvalidRequestException ex = new InvalidRequestException("test");
        ResponseEntity<Map<String, Object>> response = handler.invalidRequestExceptionException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("test", response.getBody().get("message"));
    }

    @Test
    void 인증_예외_테스트() {
        AuthException ex = new AuthException("test");
        ResponseEntity<Map<String, Object>> response = handler.handleAuthException(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("test", response.getBody().get("message"));
    }

    @Test
    void 서버_예외_테스트() {
        ServerException ex = new ServerException("test");
        ResponseEntity<Map<String, Object>> response = handler.handleServerException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("test", response.getBody().get("message"));
    }
}
