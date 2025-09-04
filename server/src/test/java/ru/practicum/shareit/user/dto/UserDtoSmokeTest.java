package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoSmokeTest {

    @Test
    void touch_UserDto_getters() {
        UserDto dto = UserDto.builder()
                .id(3L)
                .name("Bob")
                .email("b@ex.com")
                .build();

        assertEquals(3L, dto.getId());
        assertEquals("Bob", dto.getName());
        assertEquals("b@ex.com", dto.getEmail());
    }
}