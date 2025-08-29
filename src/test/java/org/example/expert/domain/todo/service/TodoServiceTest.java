package org.example.expert.domain.todo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.TodoHelper;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("Todo 저장 테스트")
    void Todo를_저장할수_있다() {
        // given
        AuthUser authUser = new AuthUser(1L, "null", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        String title = "title";
        String contents = "contents";
        String weather = "weather";
        TodoSaveRequest req = new TodoSaveRequest(title, contents);
        Todo savedTodo = new Todo(title, contents, contents, user);
        ReflectionTestUtils.setField(savedTodo, "id", 1L);

        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class))).willReturn(savedTodo);

        // when
        TodoSaveResponse resp = todoService.saveTodo(authUser, req);

        // then
        assertNotNull(resp);
        assertEquals(1L, resp.getId());
    }

    @Test
    @DisplayName("Todo 페이지네이션 테스트")
    void Todo를_페이지네이션할수_있다() {
        User user = new User("null", "null", UserRole.ADMIN);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo1 = TodoHelper.createSingleDummy(user);
        ReflectionTestUtils.setField(todo1, "id", 1L);
        ReflectionTestUtils.setField(todo1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(todo1, "modifiedAt", todo1.getCreatedAt());

        Todo todo2 = TodoHelper.createSingleDummy(user);
        ReflectionTestUtils.setField(todo2, "id", 2L);
        ReflectionTestUtils.setField(todo2, "createdAt", LocalDateTime.now().plusSeconds(1));
        ReflectionTestUtils.setField(todo2, "modifiedAt", todo2.getCreatedAt());

        Page<Todo> todoPage = new PageImpl<>(List.of(todo1, todo2));

        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class)))
                .willReturn(todoPage);

        // when
        Page<TodoResponse> resp = todoService.getTodos(1, 10);

        // then
        assertNotNull(resp);
        assertTrue(resp.hasContent());
        assertEquals(2, resp.getNumberOfElements());
        TodoResponse singleResp = resp.getContent().get(0);
        assertEquals(1L, singleResp.getId());
    }

    @Nested
    class 단일_Todo_가져오기_테스트 {
        @Test
        @DisplayName("Todo 가져오기 테스트")
        void todoId로_Todo를_가져올수_있다() {
            // given
            User user = new User("null", "null", UserRole.ADMIN);
            ReflectionTestUtils.setField(user, "id", 1L);

            Todo todo1 = TodoHelper.createSingleDummy(user);
            ReflectionTestUtils.setField(todo1, "id", 1L);
            ReflectionTestUtils.setField(todo1, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(todo1, "modifiedAt", todo1.getCreatedAt());

            given(todoRepository.findWithUserById(anyLong())).willReturn(Optional.of(todo1));

            // when
            TodoResponse resp = todoService.getTodo(1L);
            assertNotNull(resp);
            assertEquals(1L, resp.getId());
        }

        @Test
        void 조회된_Todo가_없으면_실패한다() {
            // given
            given(todoRepository.findWithUserById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class, () -> todoService.getTodo(1L));
        }
    }
}
