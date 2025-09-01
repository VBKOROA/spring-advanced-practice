package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock(lenient = true)
    private JwtUtil jwtUtil;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock(lenient = true)
    private HttpServletResponse response;

    @Mock(lenient = true)
    private PrintWriter writer;

    private MockHttpServletRequest request;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() throws IOException {
        request = new MockHttpServletRequest();
        filterChain = mock(FilterChain.class);
        given(response.getWriter()).willReturn(writer);
        willDoNothing().given(response).setStatus(anyInt());
        willDoNothing().given(response).setContentType(anyString());
        willDoNothing().given(writer).write(anyString());
    }

    @Test
    void 인증URL이면_통과() throws ServletException, IOException {
        // given
        request.setRequestURI("/auth/login");

        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void 인증헤더없음_인증필요_반환() throws ServletException, IOException {
        // given
        request.setRequestURI("/api/test");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.name());
        errorResponse.put("code", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("message", "인증이 필요합니다.");
        given(objectMapper.writeValueAsString(any())).willReturn("error");


        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(writer).write("error");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void 잘못된토큰_인증실패_401반환() throws ServletException, IOException {
        // given
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer invalid-token");
        given(jwtUtil.substringToken("Bearer invalid-token")).willReturn("invalid-token");
        given(jwtUtil.extractClaims("invalid-token")).willReturn(null);
        given(objectMapper.writeValueAsString(any())).willReturn("error");

        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(writer).write("error");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void 관리자URL_권한없음_금지_403반환() throws ServletException, IOException {
        // given
        request.setRequestURI("/admin/test");

        request.addHeader("Authorization", "Bearer user-token");
        Claims claims = new DefaultClaims();
        claims.setSubject("1");
        claims.put("userRole", UserRole.USER.name());
        given(jwtUtil.substringToken("Bearer user-token")).willReturn("user-token");
        given(jwtUtil.extractClaims("user-token")).willReturn(claims);
        given(objectMapper.writeValueAsString(any())).willReturn("error");

        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(writer).write("error");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void 유효한토큰이면_통과_속성설정() throws ServletException, IOException {
        // given
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer valid-token");
        Claims claims = new DefaultClaims();
        claims.setSubject("1");
        claims.put("email", "test@test.com");
        claims.put("userRole", UserRole.USER.name());
        given(jwtUtil.substringToken("Bearer valid-token")).willReturn("valid-token");
        given(jwtUtil.extractClaims("valid-token")).willReturn(claims);

        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(request.getAttribute("userId")).isEqualTo(1L);
        assertThat(request.getAttribute("email")).isEqualTo("test@test.com");
        assertThat(request.getAttribute("userRole")).isEqualTo(UserRole.USER.name());
    }

    @Test
    void 관리자URL_관리자권한이면_통과() throws ServletException, IOException {
        // given
        request.setRequestURI("/admin/test");
        request.addHeader("Authorization", "Bearer admin-token");
        Claims claims = new DefaultClaims();
        claims.setSubject("2");
        claims.put("email", "admin@test.com");
        claims.put("userRole", UserRole.ADMIN.name());
        given(jwtUtil.substringToken("Bearer admin-token")).willReturn("admin-token");
        given(jwtUtil.extractClaims("admin-token")).willReturn(claims);

        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(request.getAttribute("userId")).isEqualTo(2L);
        assertThat(request.getAttribute("email")).isEqualTo("admin@test.com");
        assertThat(request.getAttribute("userRole")).isEqualTo(UserRole.ADMIN.name());
    }

    @Test
    void 만료된토큰_401반환() throws ServletException, IOException {
        // given
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer expired-token");
        given(jwtUtil.substringToken("Bearer expired-token")).willReturn("expired-token");
        Claims claims = new DefaultClaims();
        claims.setSubject("1");
        given(jwtUtil.extractClaims("expired-token"))
                .willThrow(new ExpiredJwtException(null, claims, "expired"));
        given(objectMapper.writeValueAsString(any())).willReturn("error");


        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(writer).write("error");
    }

    @Test
    void 잘못된형식_400반환() throws ServletException, IOException {
        // given
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer malformed-token");
        given(jwtUtil.substringToken("Bearer malformed-token")).willReturn("malformed-token");
        given(jwtUtil.extractClaims("malformed-token"))
                .willThrow(new MalformedJwtException("malformed"));
        given(objectMapper.writeValueAsString(any())).willReturn("error");


        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(writer).write("error");
    }

    @Test
    void 지원하지않는토큰_400반환() throws ServletException, IOException {
        // given
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer unsupported-token");
        given(jwtUtil.substringToken("Bearer unsupported-token")).willReturn("unsupported-token");
        given(jwtUtil.extractClaims("unsupported-token"))
                .willThrow(new UnsupportedJwtException("unsupported"));
        given(objectMapper.writeValueAsString(any())).willReturn("error");


        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(writer).write("error");
    }

    @Test
    void 보안예외_400반환() throws ServletException, IOException {
        // given
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer security-exception-token");
        given(jwtUtil.substringToken("Bearer security-exception-token"))
                .willReturn("security-exception-token");
        given(jwtUtil.extractClaims("security-exception-token"))
                .willThrow(new SecurityException("security error"));
        given(objectMapper.writeValueAsString(any())).willReturn("error");

        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(writer).write("error");
    }

    @Test
    void 예기치않은예외_500반환() throws ServletException, IOException {
        // given
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer error-token");
        given(jwtUtil.substringToken("Bearer error-token")).willReturn("error-token");
        given(jwtUtil.extractClaims("error-token"))
                .willThrow(new RuntimeException("unexpected error"));
        given(objectMapper.writeValueAsString(any())).willReturn("error");

        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(writer).write("error");
    }
}
