package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentEntityTouchTest {

    @Test
    void touch_Comment_entity() {
        User author = new User();
        author.setId(1L);

        Item item = new Item();
        item.setId(2L);

        LocalDateTime now = LocalDateTime.now();

        Comment c = new Comment();
        c.setId(10L);
        c.setText("ok");
        c.setCreated(now);
        c.setAuthor(author);
        c.setItem(item);

        assertEquals(10L, c.getId());
        assertEquals("ok", c.getText());
        assertEquals(now, c.getCreated());
        assertEquals(1L, c.getAuthor().getId());
        assertEquals(2L, c.getItem().getId());
    }
}