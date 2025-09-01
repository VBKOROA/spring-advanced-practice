package org.example.expert.domain.comment.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentAdminService commentAdminService;

    @Test
    void Comment를_삭제할수_있다() {
        // given
        willDoNothing().given(commentRepository).deleteById(anyLong());

        // when
        commentAdminService.deleteComment(1L);

        // then
        then(commentRepository).should().deleteById(anyLong());
    }
}
