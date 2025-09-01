package org.example.expert.domain.todo.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TodoTest {
    @Test
    @DisplayName("Todo 생성 테스트")
    void Todo를_생성할수_있다() {
        // given
        String title = "title";
        String contents = "content";
        String weather = "weather";
        User user = new User("null", "null", UserRole.ADMIN);

        // when
        Todo todo = new Todo(title, contents, weather, user);

        // then
        assertNotNull(todo);
        assertEquals(todo.getTitle(), title);
        assertEquals(todo.getContents(), contents);
        assertEquals(todo.getWeather(), weather);
        assertEquals(todo.getUser(), user);
    }

    @Test
    @DisplayName("Todo 업데이트 테스트")
    void Todo의_title과_contents를_변경할수_있다() {
        // given
        String title = "title";
        String contents = "content";
        String weather = "weather";
        User user = new User("null", "null", UserRole.ADMIN);
        Todo todo = new Todo(title, contents, weather, user);

        // when
        todo.update("t", "c");

        // then
        assertEquals(todo.getContents(), "c");
        assertEquals(todo.getTitle(), "t");
    }
}
