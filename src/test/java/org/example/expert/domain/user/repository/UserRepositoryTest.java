package org.example.expert.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("User 저장 테스트")
    void User를_저장할수_있다() {
        // given
        User user = new User("null", "null", UserRole.USER);

        // when
        User savedUser = userRepository.save(user);

        // then
        assertNotNull(savedUser);
        assertThat(savedUser.getId()).isNotNull().isEqualTo(1L);
        assertEquals(savedUser.getEmail(), user.getEmail());
        assertEquals(savedUser.getPassword(), user.getPassword());
        assertEquals(savedUser.getUserRole(), user.getUserRole());
    }

    @Test
    @DisplayName("email로 User찾기 테스트")
    void User를_email로_찾을수_있다() {
        // given
        User user = new User("null", "null", UserRole.USER);
        User savedUser = userRepository.save(user);

        // when
        Optional<User> foundUserOpt = userRepository.findByEmail("null");

        // then
        assertTrue(foundUserOpt.isPresent());
        User foundUser = foundUserOpt.get();
        assertEquals(foundUser.getId(), savedUser.getId());
    }

    @Test
    @DisplayName("사용 중인 email 테스트")
    void Email을_다른User가_사용중인지_확인할수_있다() {
        // given
        User user = new User("null", "null", UserRole.USER);
        userRepository.save(user);

        // when
        boolean isUsed = userRepository.existsByEmail(user.getEmail());

        // then
        assertTrue(isUsed);
    }
}
