package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoSmokeTest {

    @Test
    void touch_ItemDto_getters() {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("ok")
                .available(true)
                .requestId(77L)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Дрель", dto.getName());
        assertEquals("ok", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(77L, dto.getRequestId());
    }

    @Test
    void touch_CommentDto_getters() {
        LocalDateTime now = LocalDateTime.now();
        CommentDto dto = CommentDto.builder()
                .id(2L)
                .text("nice")
                .authorName("Ann")
                .created(now)
                .build();

        assertEquals(2L, dto.getId());
        assertEquals("nice", dto.getText());
        assertEquals("Ann", dto.getAuthorName());
        assertEquals(now, dto.getCreated());
    }

    @Test
    void touch_CommentCreateDto_getters() {
        CommentCreateDto dto = CommentCreateDto.builder()
                .text("good")
                .build();

        assertEquals("good", dto.getText());
    }
}