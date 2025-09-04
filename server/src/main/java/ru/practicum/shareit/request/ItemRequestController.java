package ru.practicum.shareit.request;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Validated
@RestController
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestService service;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    public ItemRequestController(ItemRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_HEADER) Long userId,
                                 @RequestBody ItemRequestCreateDto body) {
        return service.create(userId, body);
    }

    @GetMapping
    public List<ItemRequestDto> my(@RequestHeader(USER_HEADER) Long userId) {
        return service.getOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> all(@RequestHeader(USER_HEADER) Long userId,
                                    @RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) {
        return service.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto byId(@RequestHeader(USER_HEADER) Long userId,
                               @PathVariable Long requestId) {
        return service.getById(userId, requestId);
    }
}