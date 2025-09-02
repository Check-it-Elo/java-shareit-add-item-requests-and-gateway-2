package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

public final class ItemRequestMapper {
    private ItemRequestMapper() {
    }

    public static ItemRequestDto toDto(ItemRequest r) {
        if (r == null) return null;
        return ItemRequestDto.builder()
                .id(r.getId())
                .description(r.getDescription())
                .created(r.getCreated())
                .build();
    }
}