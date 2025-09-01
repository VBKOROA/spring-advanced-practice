package org.example.expert.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.util.List;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
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

@WebMvcTest(CommentController.class)
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

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
    void Todo에_Comment를_작성할수_있다() throws JsonProcessingException, Exception {
        // given
        String testContents = "contents";
        UserResponse userResp = new UserResponse(authUser.getId(), authUser.getEmail());
        CommentSaveRequest commentSaveRequest = new CommentSaveRequest(testContents);
        CommentSaveResponse commentSaveResponse =
                new CommentSaveResponse(1L, testContents, userResp);

        given(commentService.saveComment(any(AuthUser.class), anyLong(),
                any(CommentSaveRequest.class))).willReturn(commentSaveResponse);

        // when & then
        mockMvc.perform(post("/todos/1/comments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentSaveRequest)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void Todo의_Comment들을_조회할수_있다() throws Exception {
        // given
        CommentResponse commentResponse1 = new CommentResponse(1L, null, null);
        CommentResponse commentResponse2 = new CommentResponse(2L, null, null);
        List<CommentResponse> commentResps = List.of(commentResponse1, commentResponse2);

        given(commentService.getComments(anyLong())).willReturn(commentResps);

        // when & then
        mockMvc.perform(get("/todos/1/comments")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}
