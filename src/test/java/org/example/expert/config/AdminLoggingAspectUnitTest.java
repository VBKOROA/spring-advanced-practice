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

import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;

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
    void 관리자API_성공_응답있음() throws Throwable {
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
    void 관리자API_성공_응답없음() throws Throwable {
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
    void 관리자API_예외_발생() throws Throwable {
        // given
        given(requestAttributes.getRequest()).willReturn(request);
        given(request.getRequestURI()).willReturn("/test");
        given(request.getAttribute("email")).willReturn("null");
        given(joinPoint.getArgs()).willReturn(new Object[] {});
        given(joinPoint.proceed()).willThrow(new RuntimeException("야호~"));

        // when & then
        assertThrows(RuntimeException.class, () -> aspect.logAdminApi(joinPoint));
    }

    @Test
    void 관리자API_요청본문_커버리지() throws Throwable {
        // given
        given(requestAttributes.getRequest()).willReturn(request);
        given(request.getRequestURI()).willReturn("/test");
        given(request.getAttribute("email")).willReturn("null");
        given(joinPoint.getArgs()).willReturn(new Object[] {null, "some string"});

        // when
        aspect.logAdminApi(joinPoint);

        // then
        then(joinPoint).should().proceed();
    }

    static class Unserializable {
        private final Unserializable self = this;

        @Override
        public String toString() {
            return self.getClass().getName();
        }
    }

    @Test
    void 관리자API_요청본문_직렬화_실패() throws Throwable {
        // given
        given(requestAttributes.getRequest()).willReturn(request);
        given(request.getRequestURI()).willReturn("/test");
        given(request.getAttribute("email")).willReturn("null");
        var map = new HashMap<String, Object>();
        map.put("key", new Unserializable());
        given(joinPoint.getArgs()).willReturn(new Object[] {map});

        // when
        aspect.logAdminApi(joinPoint);

        // then
        then(joinPoint).should().proceed();
    }

    @Test
    void 관리자API_응답_직렬화_실패() throws Throwable {
        // given
        given(requestAttributes.getRequest()).willReturn(request);
        given(request.getRequestURI()).willReturn("/test");
        given(request.getAttribute("email")).willReturn("null");
        given(joinPoint.getArgs()).willReturn(new Object[] {});
        given(joinPoint.proceed()).willReturn(new Unserializable());

        // when
        Object result = aspect.logAdminApi(joinPoint);

        // then
        assertNotNull(result);
        assertTrue(result instanceof Unserializable);
    }

    @Test
    void 관리자API_요청본문_타입일치() throws Throwable {
        // given
        given(requestAttributes.getRequest()).willReturn(request);
        given(request.getRequestURI()).willReturn("/test");
        given(request.getAttribute("email")).willReturn("null");
        var requestBody = new UserRoleChangeRequest();
        given(joinPoint.getArgs()).willReturn(new Object[] {requestBody});

        // when
        aspect.logAdminApi(joinPoint);

        // then
        then(joinPoint).should().proceed();
    }
}
