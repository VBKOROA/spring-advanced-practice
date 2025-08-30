package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.core.MethodParameter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUserArgumentResolverTest {

    @Mock
    MethodParameter param;

    @Mock
    NativeWebRequest web;

    @Mock
    HttpServletRequest req;

    @Test
    void supportsParameter_정상_지원() {
        // given
        given(param.getParameterAnnotation(Auth.class)).willReturn(mock(Auth.class));
        given(param.getParameterType()).willAnswer(invocation -> AuthUser.class);

        AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

        // when & then
        assertTrue(resolver.supportsParameter(param));
    }

    @Test
    void supportsParameter_정상_비지원() {
        // given
        given(param.getParameterAnnotation(Auth.class)).willReturn(null);
        given(param.getParameterType()).willAnswer(invocation -> String.class);

        AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

        // when & then
        assertFalse(resolver.supportsParameter(param));
    }

    @Test
    void supportsParameter_어노테이션만_있음_예외() {
        // given
        given(param.getParameterAnnotation(Auth.class)).willReturn(mock(Auth.class));
        given(param.getParameterType()).willAnswer(invocation -> String.class);

        AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

        // when & then
        assertThrows(AuthException.class, () -> resolver.supportsParameter(param));
    }

    @Test
    void supportsParameter_타입만_있음_예외() {
        // given
        given(param.getParameterAnnotation(Auth.class)).willReturn(null);
        given(param.getParameterType()).willAnswer(invocation -> AuthUser.class);

        AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

        // when & then
        assertThrows(AuthException.class, () -> resolver.supportsParameter(param));
    }

    @Test
    void resolveArgument_정상() throws Exception {
        // given
        given(web.getNativeRequest()).willReturn(req);
        given(req.getAttribute("userId")).willReturn(1L);
        given(req.getAttribute("email")).willReturn("a@b.com");
        given(req.getAttribute("userRole")).willReturn("USER");

        AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

        // when
        AuthUser user = (AuthUser) resolver.resolveArgument(null, null, web, null);

        // then
        assertEquals(1L, user.getId());
        assertEquals("a@b.com", user.getEmail());
        assertEquals(UserRole.of("USER"), user.getUserRole());
    }

    @Test
    void resolveArgument_인증정보없음_예외() throws Exception {
        // given
        given(web.getNativeRequest()).willReturn(req);
        given(req.getAttribute("userId")).willReturn(null);

        AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

        // when & then
        assertThrows(AuthException.class, () -> resolver.resolveArgument(null, null, web, null));
    }
}
