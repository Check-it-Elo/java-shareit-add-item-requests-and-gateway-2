package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;


public final class ItemMapper {
    private ItemMapper() {
    }

    // Базовый маппинг без last/next/comments
    public static ItemDto toItemDto(Item item) {
        if (item == null) return null;
        Long reqId = (item.getRequest() != null ? item.getRequest().getId() : null);
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(reqId)
                .build();
    }

    // Перегрузка для владельца: добавляем last/next и комментарии
    public static ItemDto toItemDto(Item item,
                                    BookingShortDto lastBooking,
                                    BookingShortDto nextBooking,
                                    List<CommentDto> comments) {
        ItemDto dto = toItemDto(item);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(comments);
        return dto;
    }

    public static Item toItem(ItemDto dto, User owner, ItemRequest request) {
        if (dto == null) return null;
        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable() != null ? dto.getAvailable() : false);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }

}