package org.example.expert.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import java.util.Optional;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Nested
    class User_역할변경_테스트 {
        @Test
        @DisplayName("유저 역할 변경 테스트")
        void User의_역할을_변경할수_있다() {
            // given
            User user = new User("null", "null", UserRole.USER);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            UserRoleChangeRequest req = new UserRoleChangeRequest(UserRole.ADMIN.toString());

            // when
            userAdminService.changeUserRole(1L, req);

            // then
            assertEquals(user.getUserRole(), UserRole.ADMIN);
        }

        @Test
        void User를_조회하지_못하면_실패한다() {
            // given
            UserRoleChangeRequest req = new UserRoleChangeRequest(UserRole.ADMIN.toString());

            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class,
                    () -> userAdminService.changeUserRole(1L, req));
        }
    }
}
