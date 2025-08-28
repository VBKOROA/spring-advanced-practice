package org.example.expert.domain.todo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Optional;
import org.example.expert.domain.todo.TodoHelper;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
class TodoRepositoryTest {
    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = new User("null", "null", UserRole.USER);
        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("Todo 페이지네이션 with User 테스트")
    void Todo를_수정일자_내림차순으로_정렬해서_전부_찾을수_있다() {
        // given
        Todo todo1 = TodoHelper.createSingleDummy(savedUser);
        Todo todo2 = TodoHelper.createSingleDummyWith(savedUser, "2");
        Todo todo3 = TodoHelper.createSingleDummyWith(savedUser, "3");
        todoRepository.saveAll(List.of(todo1, todo2, todo3));
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Todo> todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);

        // then
        assertNotNull(todos);
        assertTrue(todos.hasContent());
        assertEquals(3, todos.getTotalElements());
    }

    @Test
    @DisplayName("기본키로 Todo 조회 테스트")
    void 기본키로_Todo를_찾을수_있다() {
        // given
        Todo todo = TodoHelper.createSingleDummy(savedUser);
        Todo savedTodo = todoRepository.save(todo);

        // when
        Optional<Todo> todoOpt = todoRepository.findWithUserById(1L);

        // then
        assertTrue(todoOpt.isPresent());
        Todo foundTodo = todoOpt.get();
        assertEquals(savedTodo.getId(), foundTodo.getId());
    }

    @Test
    @DisplayName("기본키로 Todo 갯수 조회 테스트")
    void 기본키로_Todo의갯수를_조회할수_있다() {
        // given
        Todo todo = TodoHelper.createSingleDummy(savedUser);
        Todo savedTodo = todoRepository.save(todo);

        // when
        int count = todoRepository.countById(savedTodo.getId());

        // then
        assertEquals(1, count);
    }
}
