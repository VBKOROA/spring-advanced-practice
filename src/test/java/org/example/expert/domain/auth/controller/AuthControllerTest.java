package org.example.expert.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void 회원가입_할수있다() throws JsonProcessingException, Exception {
        // given
        SignupRequest signupRequest = new SignupRequest("null@null.com", "null", "USER");
        SignupResponse signupResponse = new SignupResponse("accessToken");

        given(authService.signup(any(SignupRequest.class))).willReturn(signupResponse);

        // when & then
        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))).andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("accessToken"));

    }

    @Test
    void 로그인_할수있다() throws JsonProcessingException, Exception {
        // given
        SigninRequest signinRequest = new SigninRequest("null@null.com", "null");
        SigninResponse signinResponse = new SigninResponse("accessToken");

        given(authService.signin(any(SigninRequest.class))).willReturn(signinResponse);

        // when & then
        mockMvc.perform(post("/auth/signin").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signinRequest))).andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("accessToken"));

    }
}
