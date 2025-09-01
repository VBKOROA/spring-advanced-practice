package org.example.expert.domain.comment.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.JsonProcessingException;

@WebMvcTest(CommentAdminController.class)
class CommentAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentAdminService commentAdminService;

    @Test
    void Todo에_Comment를_작성할수_있다() throws JsonProcessingException, Exception {
        // given
        willDoNothing().given(commentAdminService).deleteComment(anyLong());

        // when & then
        mockMvc.perform(delete("/admin/comments/1")).andExpect(status().isOk());
    }
}
