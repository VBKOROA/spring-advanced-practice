package org.example.expert.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.HashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AdminLoggingAspectUnitTest {

    private AdminLoggingAspect aspect;
    private ObjectMapper objectMapper;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        aspect = new AdminLoggingAspect(objectMapper);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Test
    void testLogAdminApi_Success_WithResponse() throws Throwable {
        // given
        given(requestAttributes.getRequest()).willReturn(request);
        given(request.getRequestURI()).willReturn("/test");
        given(request.getAttribute("email")).willReturn("null");
        given(joinPoint.getArgs()).willReturn(new Object[] {new HashMap<>()});
        given(joinPoint.proceed()).willReturn("response");

        // when
        Object result = aspect.logAdminApi(joinPoint);

        // then
        assertEquals("response", result);
        then(joinPoint).should().proceed();
    }

    @Test
    void testLogAdminApi_Success_NoResponse() throws Throwable {
        // given
        given(requestAttributes.getRequest()).willReturn(request);
        given(request.getRequestURI()).willReturn("/test");
        given(request.getAttribute("email")).willReturn(null);
        given(joinPoint.getArgs()).willReturn(new Object[] {});
        given(joinPoint.proceed()).willReturn(null);

        // when
        Object result = aspect.logAdminApi(joinPoint);

        // then
        assertNull(result);
        then(joinPoint).should().proceed();
    }

    @Test
    void testLogAdminApi_Exception() throws Throwable {
        // given
        given(requestAttributes.getRequest()).willReturn(request);
        given(request.getRequestURI()).willReturn("/test");
        given(request.getAttribute("email")).willReturn("null");
        given(joinPoint.getArgs()).willReturn(new Object[] {});
        given(joinPoint.proceed()).willThrow(new RuntimeException("야호~"));

        // when & then
        assertThrows(RuntimeException.class, () -> aspect.logAdminApi(joinPoint));
    }
}
