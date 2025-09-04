package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toItemDto_basicFields_andRequestId() {
        User owner = new User();
        owner.setId(7L);

        ItemRequest req = ItemRequest.builder().id(55L).build();

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("С ударом");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(req);

        ItemDto dto = ItemMapper.toItemDto(item);
        assertEquals(1L, dto.getId());
        assertEquals("Дрель", dto.getName());
        assertEquals("С ударом", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(55L, dto.getRequestId());
    }

    @Test
    void toItem_fromDto_setsOwnerAndRequest_andDefaultsAvailableToFalse() {
        ItemDto dto = ItemDto.builder()
                .id(123L)
                .name("Лестница")
                .description("3м")
                // available не задан — должен стать false
                .requestId(99L)
                .build();

        User owner = new User();
        owner.setId(2L);

        ItemRequest req = ItemRequest.builder().id(99L).build();

        Item item = ItemMapper.toItem(dto, owner, req);
        assertEquals(123L, item.getId());
        assertEquals("Лестница", item.getName());
        assertEquals("3м", item.getDescription());
        assertFalse(item.isAvailable());
        assertEquals(2L, item.getOwner().getId());
        assertEquals(99L, item.getRequest().getId());
    }
}