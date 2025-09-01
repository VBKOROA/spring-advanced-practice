package org.example.expert.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import java.util.Optional;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Nested
    class 회원가입_테스트 {
        @Test
        void 사용자는_회원가입_할수있다() {
            // given
            SignupRequest signupRequest = new SignupRequest("null", "null", "USER");
            User user = new User("null", "null", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(user);
            given(jwtUtil.createToken(anyLong(), anyString(), any(UserRole.class)))
                    .willReturn("accessToken");

            // when
            SignupResponse resp = authService.signup(signupRequest);

            // then
            assertEquals("accessToken", resp.getBearerToken());
        }

        @Test
        void 이미사용중인_이메일이면_회원가입_실패한다() {
            // given
            SignupRequest signupRequest = new SignupRequest("null", "null", "USER");
            given(userRepository.existsByEmail(anyString())).willReturn(true);

            // when & then
            assertThrows(InvalidRequestException.class, () -> authService.signup(signupRequest));
        }
    }

    @Nested
    class 로그인_테스트 {
        @Test
        void 사용자는_로그인할수_있다() {
            // given
            SigninRequest signinRequest = new SigninRequest("null", "null");
            User user = new User("null", "null", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
            given(jwtUtil.createToken(anyLong(), anyString(), any(UserRole.class)))
                    .willReturn("accessToken");

            // when
            SigninResponse resp = authService.signin(signinRequest);

            // then
            assertNotNull(resp);
            assertEquals("accessToken", resp.getBearerToken());
        }

        @Test
        void 없는_이메일이면_로그인_실패한다() {
            // given
            SigninRequest signinRequest = new SigninRequest("null", "null");

            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class, () -> authService.signin(signinRequest));
        }

        @Test
        void 비밀번호가_일치하지않으면_로그인_실패한다() {
            // given
            SigninRequest signinRequest = new SigninRequest("null", "null");
            User user = new User("null", "null", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when & then
            assertThrows(AuthException.class, () -> authService.signin(signinRequest));
        }
    }
}
