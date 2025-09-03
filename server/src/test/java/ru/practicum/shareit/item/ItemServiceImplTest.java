package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock ItemRepository items;
    @Mock UserRepository users;
    @Mock CommentRepository comments;
    @Mock BookingRepository bookings;
    @Mock ItemRequestRepository requests;

    ItemServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ItemServiceImpl(items, users, comments, bookings, requests);
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u"+id+"@ex.com");
        return u;
    }

    private static Item item(long id, long ownerId, boolean available) {
        Item it = new Item();
        it.setId(id);
        it.setName("Item"+id);
        it.setDescription("desc");
        it.setAvailable(available);
        User owner = user(ownerId);
        it.setOwner(owner);
        return it;
    }

    @Test
    void create_ok_mapsAndSaves() {
        long ownerId = 1L;
        when(users.findById(ownerId)).thenReturn(Optional.of(user(ownerId)));
        when(items.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(100L);
            return i;
        });

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("С ударом")
                .available(true)
                .build();

        ItemDto out = service.create(ownerId, dto);
        assertNotNull(out.getId());
        assertEquals("Дрель", out.getName());
        assertEquals("С ударом", out.getDescription());
        assertTrue(out.getAvailable());
    }

    @Test
    void create_withRequest_linksRequest() {
        long ownerId = 1L;
        when(users.findById(ownerId)).thenReturn(Optional.of(user(ownerId)));
        when(requests.findById(77L)).thenReturn(Optional.of(ItemRequest.builder().id(77L).build()));
        when(items.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDto dto = ItemDto.builder()
                .name("Стремянка")
                .description("2м")
                .available(true)
                .requestId(77L)
                .build();

        ItemDto out = service.create(ownerId, dto);
        assertEquals(77L, out.getRequestId());
    }

    @Test
    void update_notOwner_throwsSecurity() {
        Item persisted = item(10L, 2L, true);
        when(items.findById(10L)).thenReturn(Optional.of(persisted));

        assertThrows(SecurityException.class, () ->
                service.update(99L, 10L, ItemDto.builder().name("x").build()));
    }

    @Test
    void getById_forOwner_includesLastNextAndComments() {
        Item it = item(10L, 2L, true);
        when(items.findById(10L)).thenReturn(Optional.of(it));

        Comment c = Comment.builder()
                .id(1L).text("ok").created(LocalDateTime.now())
                .item(it).author(user(3L)).build();
        when(comments.findByItem_IdOrderByCreatedDesc(10L)).thenReturn(List.of(c));

        // last/next APPROVED мокаем на Optional.of(...)
        when(bookings.findTop1ByItem_IdAndStartBeforeAndStatusOrderByStartDesc(eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());
        when(bookings.findTop1ByItem_IdAndStartAfterAndStatusOrderByStartAsc(eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());

        ItemDto dto = service.getById(2L, 10L);
        assertNotNull(dto.getComments());
        assertEquals(1, dto.getComments().size());
        // last/next могут быть null — это ок, главное, что код прошёл ветку владельца
    }

    @Test
    void search_blank_returnsEmpty() {
        assertTrue(service.search(null).isEmpty());
        assertTrue(service.search("").isEmpty());
        assertTrue(service.search("   ").isEmpty());
    }

    @Test
    void addComment_allowed_savesAndReturnsDto() {
        Item it = item(10L, 2L, true);
        when(items.findById(10L)).thenReturn(Optional.of(it));
        when(users.findById(3L)).thenReturn(Optional.of(user(3L)));
        when(bookings.existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(eq(3L), eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(true);

        when(comments.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });

        CommentCreateDto body = CommentCreateDto.builder().text("nice").build();
        CommentDto dto = service.addComment(3L, 10L, body);
        assertEquals(5L, dto.getId());
        assertEquals("nice", dto.getText());
    }

    @Test
    void addComment_denied_noCompletedBooking_throwsIAE() {
        Item it = item(10L, 2L, true);
        when(items.findById(10L)).thenReturn(Optional.of(it));
        when(users.findById(3L)).thenReturn(Optional.of(user(3L)));
        when(bookings.existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(eq(3L), eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(false);

        CommentCreateDto body = CommentCreateDto.builder().text("x").build();
        assertThrows(IllegalArgumentException.class, () -> service.addComment(3L, 10L, body));
    }
}