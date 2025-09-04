package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository items;
    private final UserRepository users;
    private final CommentRepository comments;
    private final BookingRepository bookings;
    private final ItemRequestRepository itemRequests;

    public ItemServiceImpl(ItemRepository items,
                           UserRepository users,
                           CommentRepository comments,
                           BookingRepository bookings,
                           ItemRequestRepository itemRequests) {
        this.items = items;
        this.users = users;
        this.comments = comments;
        this.bookings = bookings;
        this.itemRequests = itemRequests;
    }

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto dto) {
        validateItemForCreate(dto);

        User owner = users.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + ownerId));

        // Если передан requestId — подтянем заявку и свяжем вещь
        ItemRequest req = null;
        if (dto.getRequestId() != null) {
            req = itemRequests.findById(dto.getRequestId())
                    .orElseThrow(() -> new NoSuchElementException("Request not found: " + dto.getRequestId()));
        }

        Item item = ItemMapper.toItem(dto, owner, req);
        item.setId(null);
        item = items.save(item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto patch) {
        Item item = items.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found: " + itemId));

        if (!Objects.equals(item.getOwner().getId(), ownerId)) {
            throw new SecurityException("Only owner can edit the item: " + itemId);
        }

        if (patch.getName() != null) item.setName(patch.getName());
        if (patch.getDescription() != null) item.setDescription(patch.getDescription());
        if (patch.getAvailable() != null) item.setAvailable(patch.getAvailable());

        if (patch.getRequestId() != null) {
            ItemRequest req = itemRequests.findById(patch.getRequestId())
                    .orElseThrow(() -> new NoSuchElementException("Request not found: " + patch.getRequestId()));
            item.setRequest(req);
        }

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getById(Long requesterId, Long itemId) {
        Item item = items.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found: " + itemId));

        // комментарии всегда
        List<CommentDto> commentDtos = comments.findByItem_IdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toDto)
                .toList();

        // last/next — показываем ТОЛЬКО владельцу
        BookingShortDto last = null;
        BookingShortDto next = null;
        if (Objects.equals(item.getOwner().getId(), requesterId)) {
            LocalDateTime now = LocalDateTime.now();
            last = bookings
                    .findTop1ByItem_IdAndStartBeforeAndStatusOrderByStartDesc(itemId, now, BookingStatus.APPROVED)
                    .map(b -> BookingShortDto.builder().id(b.getId()).bookerId(b.getBooker().getId()).build())
                    .orElse(null);
            next = bookings
                    .findTop1ByItem_IdAndStartAfterAndStatusOrderByStartAsc(itemId, now, BookingStatus.APPROVED)
                    .map(b -> BookingShortDto.builder().id(b.getId()).bookerId(b.getBooker().getId()).build())
                    .orElse(null);
        }

        return ItemMapper.toItemDto(item, last, next, commentDtos);
    }

    @Override
    public List<ItemDto> getOwnerItems(Long ownerId) {
        List<Item> owned = items.findByOwner_IdOrderByIdAsc(ownerId);
        if (owned.isEmpty()) return List.of();

        var itemIds = owned.stream().map(Item::getId).toList();

        // комментарии пачкой и группируем по itemId
        var commentsByItemId = comments.findByItem_IdInOrderByCreatedDesc(itemIds).stream()
                .collect(java.util.stream.Collectors.groupingBy(c -> c.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        return owned.stream().map(i -> {
            var last = bookings
                    .findTop1ByItem_IdAndStartBeforeAndStatusOrderByStartDesc(i.getId(), now, BookingStatus.APPROVED)
                    .map(b -> BookingShortDto.builder()
                            .id(b.getId()).bookerId(b.getBooker().getId()).build())
                    .orElse(null);

            var next = bookings
                    .findTop1ByItem_IdAndStartAfterAndStatusOrderByStartAsc(i.getId(), now, BookingStatus.APPROVED)
                    .map(b -> BookingShortDto.builder()
                            .id(b.getId()).bookerId(b.getBooker().getId()).build())
                    .orElse(null);

            var commentDtos = commentsByItemId.getOrDefault(i.getId(), List.of()).stream()
                    .map(CommentMapper::toDto)
                    .toList();

            return ItemMapper.toItemDto(i, last, next, commentDtos);
        }).toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) return List.of();
        return items.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto) {
        if (dto == null || dto.getText() == null || dto.getText().isBlank()) {
            throw new IllegalArgumentException("Comment text must not be blank");
        }

        Item item = items.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found: " + itemId));
        User author = users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        LocalDateTime now = LocalDateTime.now();
        boolean allowed = bookings.existsPastBookingForComment(itemId, userId, now);

        if (!allowed) {
            throw new IllegalArgumentException("User has no completed booking of this item");
        }

        Comment c = Comment.builder()
                .text(dto.getText())
                .created(now)
                .item(item)
                .author(author)
                .build();

        c = comments.save(c);
        return CommentMapper.toDto(c);
    }

    private void validateItemForCreate(ItemDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Item body must not be null");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Item name must not be null or blank");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Item description must not be null or blank");
        }
        if (dto.getAvailable() == null) {
            throw new IllegalArgumentException("Item available flag must not be null");
        }
    }

}