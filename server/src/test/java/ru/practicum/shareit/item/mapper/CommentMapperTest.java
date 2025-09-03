package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    @Test
    void toDto_mapsAllFields() {
        User author = new User();
        author.setId(5L);
        author.setName("Bob");

        Item item = new Item();
        item.setId(10L);

        Comment c = Comment.builder()
                .id(77L)
                .text("ok")
                .created(LocalDateTime.of(2025, 1, 2, 3, 4, 5))
                .item(item)
                .author(author)
                .build();

        CommentDto dto = CommentMapper.toDto(c);
        assertEquals(77L, dto.getId());
        assertEquals("ok", dto.getText());
        assertEquals("Bob", dto.getAuthorName());
        assertEquals(LocalDateTime.of(2025, 1, 2, 3, 4, 5), dto.getCreated());
    }

    @Test
    void toDto_null_returnsNull() {
        assertNull(CommentMapper.toDto(null));
    }
}