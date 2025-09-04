package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoClassesTouchTest {

    @Test
    void touch_ItemDto() {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Дрель");
        dto.setDescription("ok");
        dto.setAvailable(true);
        dto.setRequestId(77L);

        assertEquals(1L, dto.getId());
        assertEquals("Дрель", dto.getName());
        assertEquals("ok", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(77L, dto.getRequestId());
    }

    @Test
    void touch_CommentDto() {
        CommentDto dto = new CommentDto();
        dto.setId(2L);
        dto.setText("nice");
        dto.setAuthorName("Ann");
        LocalDateTime now = LocalDateTime.now();
        dto.setCreated(now);

        assertEquals(2L, dto.getId());
        assertEquals("nice", dto.getText());
        assertEquals("Ann", dto.getAuthorName());
        assertEquals(now, dto.getCreated());
    }

    @Test
    void touch_CommentCreateDto() {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setText("good");
        assertEquals("good", dto.getText());
    }
}