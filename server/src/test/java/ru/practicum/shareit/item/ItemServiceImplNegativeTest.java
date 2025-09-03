package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplNegativeTest {

    private ItemRepository items;
    private UserRepository users;
    private CommentRepository comments;
    private BookingRepository bookings;
    private ItemRequestRepository itemRequests;

    private ItemServiceImpl service;

    @BeforeEach
    void setUp() {
        items = mock(ItemRepository.class);
        users = mock(UserRepository.class);
        comments = mock(CommentRepository.class);
        bookings = mock(BookingRepository.class);
        itemRequests = mock(ItemRequestRepository.class);
        service = new ItemServiceImpl(items, users, comments, bookings, itemRequests);
    }

    // ---------- create ----------

    @Test
    void create_nullBody_throwsIAE() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(1L, null));
        assertTrue(ex.getMessage().contains("Item body must not be null"));
    }

    @Test
    void create_blankName_throwsIAE() {
        ItemDto dto = ItemDto.builder()
                .name("  ")
                .description("d")
                .available(true)
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(1L, dto));
        assertTrue(ex.getMessage().contains("Item name"));
    }

    @Test
    void create_blankDescription_throwsIAE() {
        ItemDto dto = ItemDto.builder()
                .name("N")
                .description(" ")
                .available(true)
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(1L, dto));
        assertTrue(ex.getMessage().contains("Item description"));
    }

    @Test
    void create_availableNull_throwsIAE() {
        ItemDto dto = ItemDto.builder()
                .name("N")
                .description("d")
                .available(null)
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(1L, dto));
        assertTrue(ex.getMessage().contains("available"));
    }

    @Test
    void create_ownerNotFound_throwsNSE() {
        when(users.findById(9L)).thenReturn(Optional.empty());

        ItemDto dto = ItemDto.builder()
                .name("N")
                .description("d")
                .available(true)
                .build();

        assertThrows(NoSuchElementException.class, () -> service.create(9L, dto));
    }

    @Test
    void create_requestNotFound_throwsNSE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRequests.findById(100L)).thenReturn(Optional.empty());

        ItemDto dto = ItemDto.builder()
                .name("N")
                .description("d")
                .available(true)
                .requestId(100L)
                .build();

        assertThrows(NoSuchElementException.class, () -> service.create(1L, dto));
    }

    // ---------- update ----------

    @Test
    void update_itemNotFound_throwsNSE() {
        when(items.findById(5L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> service.update(1L, 5L, ItemDto.builder().build()));
    }

    @Test
    void update_notOwner_throwsSecurity() {
        User owner = user(10L);
        Item item = new Item();
        item.setId(7L);
        item.setOwner(owner);
        when(items.findById(7L)).thenReturn(Optional.of(item));

        assertThrows(SecurityException.class,
                () -> service.update(99L, 7L, ItemDto.builder().name("x").build()));
        verify(items, never()).save(any());
    }

    @Test
    void update_requestNotFound_throwsNSE() {
        User owner = user(1L);
        Item item = new Item();
        item.setId(7L);
        item.setOwner(owner);
        when(items.findById(7L)).thenReturn(Optional.of(item));
        when(itemRequests.findById(500L)).thenReturn(Optional.empty());

        ItemDto patch = ItemDto.builder().requestId(500L).build();
        assertThrows(NoSuchElementException.class,
                () -> service.update(1L, 7L, patch));
    }

    // ---------- search ----------

    @Test
    void search_blank_returnsEmptyList() {
        assertTrue(service.search("   ").isEmpty());
        verify(items, never()).search(any());
    }

    // ---------- addComment ----------

    @Test
    void addComment_blankText_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addComment(1L, 2L, CommentCreateDto.builder().text(" ").build()));
    }

    @Test
    void addComment_itemNotFound_throwsNSE() {
        when(items.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> service.addComment(1L, 2L, CommentCreateDto.builder().text("ok").build()));
    }

    @Test
    void addComment_userNotFound_throwsNSE() {
        Item it = new Item();
        it.setId(2L);
        when(items.findById(2L)).thenReturn(Optional.of(it));
        when(users.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.addComment(1L, 2L, CommentCreateDto.builder().text("ok").build()));
    }

    @Test
    void addComment_noCompletedBooking_throwsIAE() {
        Item it = new Item();
        it.setId(2L);
        it.setOwner(user(10L));
        when(items.findById(2L)).thenReturn(Optional.of(it));
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(bookings.existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                eq(1L), eq(2L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.addComment(1L, 2L, CommentCreateDto.builder().text("ok").build()));
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@ex.com");
        return u;
    }
}