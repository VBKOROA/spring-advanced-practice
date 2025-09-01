package org.example.expert.domain.todo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.util.List;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;

@WebMvcTest(TodoController.class)
class TodoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    @Autowired
    private ObjectMapper objectMapper;

    private final AuthUser authUser = new AuthUser(1L, "null", UserRole.USER);

    @BeforeEach
    void setUp() throws IOException, ServletException {
        given(authUserArgumentResolver.supportsParameter(any())).willAnswer(invocation -> {
            MethodParameter param = invocation.getArgument(0);
            return param.getParameterAnnotation(Auth.class) != null;
        });
        given(authUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .willReturn(authUser);
    }

    @Test
    @DisplayName("새 Todo 작성 테스트")
    void Todo를_작성할수_있다() throws JsonProcessingException, Exception {
        // given
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contents");
        TodoSaveResponse todoSaveResponse =
                new TodoSaveResponse(1L, "title", "contents", "weather", null);

        given(todoService.saveTodo(authUser, todoSaveRequest)).willReturn(todoSaveResponse);

        // when & then
        mockMvc.perform(post("/todos").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todoSaveRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Todo 페이지네이션 테스트")
    void Todo를_페이지네이션할수_있다() throws Exception {
        // given
        TodoResponse todoResponse = new TodoResponse(1L, "null", "null", "null", null, null, null);
        Page<TodoResponse> todoRespPage = new PageImpl<>(List.of(todoResponse));

        given(todoService.getTodos(1, 10)).willReturn(todoRespPage);

        // when & then
        mockMvc.perform(get("/todos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void todoId로_Todo를_조회할수_있다() throws Exception {
        // given
        TodoResponse todoResponse = new TodoResponse(1L, "null", "null", "null", null, null, null);

        given(todoService.getTodo(1L)).willReturn(todoResponse);

        // when & then
        mockMvc.perform(get("/todos/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoResponse.getId()));
    }
}
