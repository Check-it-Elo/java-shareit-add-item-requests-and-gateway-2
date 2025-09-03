package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestDtoSmokeTest {

    @Test
    void touch_ItemRequestCreateDto_getters() {
        ItemRequestCreateDto c = ItemRequestCreateDto.builder()
                .description("Нужна дрель")
                .build();

        assertEquals("Нужна дрель", c.getDescription());
    }

    @Test
    void touch_ItemRequestDto_getters() {
        LocalDateTime now = LocalDateTime.now();

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(now)
                .items(List.of())
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Нужна дрель", dto.getDescription());
        assertEquals(now, dto.getCreated());
        assertNotNull(dto.getItems());
    }
}