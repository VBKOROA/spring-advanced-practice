package org.example.expert.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import java.util.Optional;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    class 단일_유저조회_테스트 {
        @Test
        @DisplayName("기본키로 User 가져오기 테스트")
        void userId로_User를_가져올수_있다() {
            // given
            User user = new User("null", "null", UserRole.USER);

            ReflectionTestUtils.setField(user, "id", 1L);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            UserResponse userResponse = userService.getUser(1L);

            // then
            assertNotNull(userResponse);
            assertEquals(userResponse.getId(), user.getId());
            assertEquals(userResponse.getEmail(), user.getEmail());
        }

        @Test
        void 존재하지않는_userId면_실패한다() {
            // given
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class, () -> userService.getUser(1L));
        }
    }


    @Nested
    class 비밀번호_변경_테스트 {
        private final String oldPassword = "Null1234!";
        private final String newPassword = "NewNull1234!";

        @Test
        @DisplayName("비밀번호 변경 테스트")
        void 비밀번호를_변경할수_있다() {
            // given
            User user = new User("null", oldPassword, UserRole.USER);
            UserChangePasswordRequest req = new UserChangePasswordRequest(oldPassword, newPassword);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(newPassword, oldPassword)).willReturn(false);
            given(passwordEncoder.matches(oldPassword, oldPassword)).willReturn(true);
            given(passwordEncoder.encode(newPassword)).willReturn(newPassword);

            // when & then
            assertDoesNotThrow(() -> userService.changePassword(1L, req));
            assertEquals(user.getPassword(), newPassword);
        }

        @DisplayName("비밀번호 변경 - 유효성 실패")
        @ParameterizedTest
        @ValueSource(strings = {"k", "aaaaaaaa", "1aaaaaaa"})
        void 비밀번호가_유효하지않으면_예외를_던진다(String newPassword) {
            // given
            UserChangePasswordRequest req = new UserChangePasswordRequest(oldPassword, newPassword);

            // when & then
            assertThrows(InvalidRequestException.class, () -> userService.changePassword(1L, req));
        }

        @Test
        void 조회된_User가_없으면_실패한다() {
            // given
            UserChangePasswordRequest req = new UserChangePasswordRequest(oldPassword, newPassword);

            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class, () -> userService.changePassword(1L, req));
        }

        @Test
        @DisplayName("비밀번호 변경 - 기존 비밀번호 사용")
        void 새비밀번호가_기존의_비밀번호면_예외를_던진다() {
            // given
            String newPassword = oldPassword;
            User user = new User("null", oldPassword, UserRole.USER);
            UserChangePasswordRequest req = new UserChangePasswordRequest(oldPassword, newPassword);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(newPassword, oldPassword)).willReturn(true);

            // when & then
            assertThrows(InvalidRequestException.class, () -> userService.changePassword(1L, req));
        }

        @Test
        @DisplayName("비밀번호 변경 - 비밀번호 불일치")
        void 기존의_비밀번호와_불일치하면_예외를_던진다() {
            // given
            String reqOldPassword = "WrongNull1234!";

            User user = new User("null", oldPassword, UserRole.USER);
            UserChangePasswordRequest req =
                    new UserChangePasswordRequest(reqOldPassword, newPassword);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(newPassword, oldPassword)).willReturn(false);
            given(passwordEncoder.matches(reqOldPassword, oldPassword)).willReturn(false);

            // when & then
            assertThrows(InvalidRequestException.class, () -> userService.changePassword(1L, req));
        }
    }
}
