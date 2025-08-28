package org.example.expert.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;

@WebMvcTest(UserController.class)
class UserControllerTest {
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

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

        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setCustomArgumentResolvers(authUserArgumentResolver).build();
    }

    @Test
    @DisplayName("user 정보 가져오기")
    void userId로_User정보를_가져올수_있다() throws Exception {
        // given
        UserResponse resp = new UserResponse(1L, "null");
        given(userService.getUser(anyLong())).willReturn(resp);

        // when & then
        mockMvc.perform(get("/users/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.email").value("null"));
    }

    @Test
    @DisplayName("user 비밀번호 변경하기")
    void user의_비밀번호를_변경할수_있다() throws JsonProcessingException, Exception {
        // given
        UserChangePasswordRequest req =
                new UserChangePasswordRequest("oldPassword", "newPassword1234!");

        willDoNothing().given(userService).changePassword(anyLong(),
                any(UserChangePasswordRequest.class));

        // when & then
        mockMvc.perform(put("/users").header("Authorization", "Bearer mock header")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))).andExpect(status().isOk());
    }
}
