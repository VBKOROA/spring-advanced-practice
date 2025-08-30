package org.example.expert.config;

import io.jsonwebtoken.Claims;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey",
                "secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey");
        jwtUtil.init();
    }

    @Test
    void 토큰_생성에_성공한다() {
        // given
        Long userId = 1L;
        String email = "test@example.com";
        UserRole userRole = UserRole.USER;

        // when
        String token = jwtUtil.createToken(userId, email, userRole);

        // then
        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "));
    }

    @Test
    void Bearer_토큰_추출에_성공한다() {
        // given
        String tokenValue = "Bearer validtoken";

        // when
        String result = jwtUtil.substringToken(tokenValue);

        // then
        assertEquals("validtoken", result);
    }

    @Test
    void Bearer가_없는_토큰이면_예외가_발생한다() {
        // given
        String tokenValue = "invalidtoken";

        // when & then
        assertThrows(ServerException.class, () -> {
            jwtUtil.substringToken(tokenValue);
        });
    }

    @Test
    void 빈_토큰이면_예외가_발생한다() {
        // given
        String tokenValue = "";

        // when & then
        assertThrows(ServerException.class, () -> {
            jwtUtil.substringToken(tokenValue);
        });
    }

    @Test
    void 토큰에서_Claims_추출에_성공한다() {
        // given
        Long userId = 1L;
        String email = "test@example.com";
        UserRole userRole = UserRole.USER;
        String fullToken = jwtUtil.createToken(userId, email, userRole);
        String token = jwtUtil.substringToken(fullToken);

        // when
        Claims claims = jwtUtil.extractClaims(token);

        // then
        assertEquals("1", claims.getSubject());
        assertEquals(email, claims.get("email"));
        assertEquals(userRole.toString(), claims.get("userRole"));
    }
}
