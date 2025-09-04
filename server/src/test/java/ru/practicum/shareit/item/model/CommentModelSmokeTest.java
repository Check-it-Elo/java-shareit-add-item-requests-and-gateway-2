package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentModelSmokeTest {

    @Test
    void touch_Comment_entity_buildersAndGetters() {
        User u = new User();
        u.setId(1L);
        Item it = new Item();
        it.setId(2L);

        LocalDateTime now = LocalDateTime.now();

        Comment c = Comment.builder()
                .id(10L)
                .text("ok")
                .created(now)
                .author(u)
                .item(it)
                .build();

        assertEquals(10L, c.getId());
        assertEquals("ok", c.getText());
        assertEquals(now, c.getCreated());
        assertEquals(1L, c.getAuthor().getId());
        assertEquals(2L, c.getItem().getId());
    }
}