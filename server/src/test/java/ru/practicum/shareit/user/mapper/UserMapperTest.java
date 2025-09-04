package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserDto_mapsAll() {
        User u = new User();
        u.setId(1L);
        u.setName("Bob");
        u.setEmail("b@ex.com");

        UserDto dto = UserMapper.toUserDto(u);
        assertEquals(1L, dto.getId());
        assertEquals("Bob", dto.getName());
        assertEquals("b@ex.com", dto.getEmail());
    }

    @Test
    void toUser_mapsAll() {
        UserDto dto = UserDto.builder().id(5L).name("Ann").email("a@ex.com").build();
        User u = UserMapper.toUser(dto);
        assertEquals(5L, u.getId());
        assertEquals("Ann", u.getName());
        assertEquals("a@ex.com", u.getEmail());
    }
}