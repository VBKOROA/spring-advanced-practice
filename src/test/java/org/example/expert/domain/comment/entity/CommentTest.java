package org.example.expert.domain.comment.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class CommentTest {
    @Test
    void Comment를_생성할수_있다() {
        // given

        // when
        Comment comment = new Comment("contents", null, null);

        // then
        assertNotNull(comment);
    }

    @Test
    void Comment의_내용을_변경할수_있다() {
        // given
        Comment comment = new Comment("contents", null, null);

        // when
        comment.update("updatedContents");

        // then
        assertEquals("updatedContents", comment.getContents());
    }
}
