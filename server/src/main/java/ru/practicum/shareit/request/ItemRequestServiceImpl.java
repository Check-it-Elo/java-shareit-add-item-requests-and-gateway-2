package ru.practicum.shareit.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requests;
    private final UserRepository users;
    private final ItemRepository items;

    public ItemRequestServiceImpl(ItemRequestRepository requests,
                                  UserRepository users,
                                  ItemRepository items) {
        this.requests = requests;
        this.users = users;
        this.items = items;
    }

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        if (dto == null || dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        User requestor = users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        ItemRequest r = ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
        r = requests.save(r);
        return ItemRequestMapper.toDto(r);
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        ensureUser(userId);
        List<ItemRequest> list = requests.findByRequestor_IdOrderByCreatedDesc(userId);
        return enrichWithItems(list);
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        ensureUser(userId);
        if (from < 0 || size <= 0) throw new IllegalArgumentException("Invalid pagination");
        Pageable page = PageRequest.of(from / size, size);
        var pageRes = requests.findByRequestor_IdNotOrderByCreatedDesc(userId, page);
        return enrichWithItems(pageRes.getContent());
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        ensureUser(userId);
        ItemRequest r = requests.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Request not found: " + requestId));
        ItemRequestDto dto = ItemRequestMapper.toDto(r);
        var related = items.findByRequest_Id(requestId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
        dto.setItems(related);
        return dto;
    }

    private List<ItemRequestDto> enrichWithItems(List<ItemRequest> list) {
        var dtos = list.stream().map(ItemRequestMapper::toDto).toList();
        var requestIds = list.stream().map(ItemRequest::getId).toList();

        var itemsByRequest = items.findByRequest_IdIn(requestIds)
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        i -> i.getRequest().getId()
                ));

        return dtos.stream().map(d -> {
            var related = itemsByRequest.getOrDefault(d.getId(), List.of())
                    .stream().map(ItemMapper::toItemDto).toList();
            d.setItems(related);
            return d;
        }).toList();
    }

    private void ensureUser(Long userId) {
        if (!users.existsById(userId)) {
            throw new NoSuchElementException("User not found: " + userId);
        }
    }

}