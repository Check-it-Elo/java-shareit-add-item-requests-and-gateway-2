package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;

public interface ItemService {

    ItemDto create(Long ownerId, ItemDto dto);

    ItemDto update(Long ownerId, Long itemId, ItemDto patch);

    ItemDto getById(Long requesterId, Long itemId);

    List<ItemDto> getOwnerItems(Long ownerId);

    List<ItemDto> search(String text); // возвращать только доступные

    CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto);

}