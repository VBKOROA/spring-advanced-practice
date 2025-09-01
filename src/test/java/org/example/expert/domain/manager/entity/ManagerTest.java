package org.example.expert.domain.manager.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.junit.jupiter.api.Test;

class ManagerTest {
    @Test
    void Manager를_생성할수_있다() {
        // given
        User user = new User("null", "null", null);
        Todo todo = new Todo("null", "null", "null", user);

        // when
        Manager manager = new Manager(user, todo);

        // then
        assertNotNull(manager);
        assertEquals(todo, manager.getTodo());
        assertEquals(user, manager.getUser());
    }
}
