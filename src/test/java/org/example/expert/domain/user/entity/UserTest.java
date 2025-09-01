package org.example.expert.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {
    @Test
    @DisplayName("유저 생성자 테스트")
    void User_생성_성공() {
        // given
        String testEmail = "email@email.com";
        String testPassword = "password";

        // when
        User user = new User(testEmail, testPassword, UserRole.USER);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(testEmail);
        assertThat(user.getPassword()).isEqualTo(testPassword);
        assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("AuthUser -> User 변환 테스트")
    void AuthUser를_User로_변환할수_있다() {
        // given
        String testEmail = "email@email.com";
        Long testId = 1L;

        AuthUser authUser = new AuthUser(testId, testEmail, UserRole.USER);

        // when
        User user = User.fromAuthUser(authUser);

        // then
        assertNotNull(user);
        assertEquals(user.getId(), authUser.getId());
        assertEquals(user.getEmail(), authUser.getEmail());
        assertEquals(user.getUserRole(), authUser.getUserRole());
    }

    @Test
    @DisplayName("User 비밀번호 변경 테스트")
    void User는_비밀번호를_변경할수_있다() {
        // given
        User user = new User("email", "password", UserRole.USER);
        String newPassword = "changedPassword";

        // when
        user.changePassword(newPassword);

        // then
        assertEquals(user.getPassword(), newPassword);
    }

    @Test
    @DisplayName("User 역할 변경 테스트")
    void User는_역할을_변경할수_있다() {
        // given
        User user = new User("email", "password", UserRole.USER);

        // when
        user.updateRole(UserRole.ADMIN);

        // then
        assertEquals(user.getUserRole(), UserRole.ADMIN);
    }
}
