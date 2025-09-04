package ru.practicum.shareit.item;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Validated
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService items;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    public ItemController(ItemService items) {
        this.items = items;
    }

    @PostMapping
    public ItemDto create(@RequestHeader(USER_HEADER) Long ownerId,
                          @RequestBody ItemDto body) {
        return items.create(ownerId, body);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_HEADER) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto patch) {
        return items.update(ownerId, itemId, patch);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(USER_HEADER) Long requesterId,
                           @PathVariable Long itemId) {
        return items.getById(requesterId, itemId);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader(USER_HEADER) Long ownerId) {
        return items.getOwnerItems(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return items.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_HEADER) Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentCreateDto body) {
        return items.addComment(userId, itemId, body);
    }
}