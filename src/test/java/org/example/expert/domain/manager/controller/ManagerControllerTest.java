package org.example.expert.domain.manager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.util.List;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;

@WebMvcTest(ManagerController.class)
class ManagerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ManagerService managerService;

    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

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
    void Todo에_담당자를_등록한다() throws JsonProcessingException, Exception {
        // given
        final long todoId = 1L;
        final ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(2L);
        final ManagerSaveResponse managerSaveResponse =
                new ManagerSaveResponse(1L, new UserResponse(2L, "null"));

        given(managerService.saveManager(any(AuthUser.class), eq(todoId),
                any(ManagerSaveRequest.class))).willReturn(managerSaveResponse);

        // when & then
        mockMvc.perform(
                post("/todos/" + todoId + "/managers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(managerSaveRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void Todo의_담당자들을_조회할수_있다() throws Exception {
        // given
        UserResponse userResponse = new UserResponse(1L, "null");
        ManagerResponse managerResponse = new ManagerResponse(1L, userResponse);
        List<ManagerResponse> managers = List.of(managerResponse);

        given(managerService.getManagers(eq(1L))).willReturn(managers);

        // when & then
        mockMvc.perform(get("/todos/1/managers")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void Todo의_담당자를_제거할수_있다() throws Exception {
        // given
        willDoNothing().given(managerService).deleteManager(anyLong(), anyLong(), anyLong());

        // when & then
        mockMvc.perform(delete("/todos/1/managers/1")).andExpect(status().isOk());
    }
}
