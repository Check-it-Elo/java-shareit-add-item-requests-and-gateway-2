package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplNegativeTest {

    private BookingRepository bookings;
    private UserRepository users;
    private ItemRepository items;

    private BookingServiceImpl service;

    @BeforeEach
    void setUp() {
        bookings = mock(BookingRepository.class);
        users = mock(UserRepository.class);
        items = mock(ItemRepository.class);
        service = new BookingServiceImpl(bookings, users, items);
    }

    // ---------- create ----------

    @Test
    void create_nullBody_throwsIAE() {
        assertThrows(IllegalArgumentException.class, () -> service.create(1L, null));
    }

    @Test
    void create_itemIdNull_throwsIAE() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
        assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_startAfterEnd_throwsIAE() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(1))
                .build();
        assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_userNotFound_throwsNSE() {
        when(users.findById(1L)).thenReturn(Optional.empty());

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(NoSuchElementException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_itemNotFound_throwsNSE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(items.findById(2L)).thenReturn(Optional.empty());

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(NoSuchElementException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_itemNotAvailable_throwsIAE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        Item it = new Item();
        it.setId(2L);
        it.setOwner(user(99L));
        it.setAvailable(false);
        when(items.findById(2L)).thenReturn(Optional.of(it));

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_ownerBooksOwnItem_throwsNSE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        Item it = new Item();
        it.setId(2L);
        it.setOwner(user(1L));
        it.setAvailable(true);
        when(items.findById(2L)).thenReturn(Optional.of(it));

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(NoSuchElementException.class, () -> service.create(1L, dto));
    }

    // ---------- approve ----------

    @Test
    void approve_bookingNotFound_throwsNSE() {
        when(bookings.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.approve(1L, 9L, true));
    }

    @Test
    void approve_notOwner_throwsSecurity() {
        Booking b = new Booking();
        Item it = new Item();
        it.setOwner(user(10L));
        b.setItem(it);
        b.setStatus(BookingStatus.WAITING);
        when(bookings.findById(7L)).thenReturn(Optional.of(b));

        assertThrows(SecurityException.class, () -> service.approve(99L, 7L, true));
    }

    @Test
    void approve_notWaiting_throwsIllegalState() {
        Booking b = new Booking();
        Item it = new Item();
        it.setOwner(user(1L));
        b.setItem(it);
        b.setStatus(BookingStatus.APPROVED);
        when(bookings.findById(7L)).thenReturn(Optional.of(b));

        assertThrows(IllegalStateException.class, () -> service.approve(1L, 7L, true));
    }

    // ---------- getById ----------

    @Test
    void getById_bookingNotFound_throwsNSE() {
        when(bookings.findById(5L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.getById(1L, 5L));
    }

    @Test
    void getById_notBookerAndNotOwner_throwsSecurity() {
        Booking b = new Booking();
        b.setBooker(user(2L));
        Item it = new Item();
        it.setOwner(user(3L));
        b.setItem(it);
        when(bookings.findById(5L)).thenReturn(Optional.of(b));

        assertThrows(SecurityException.class, () -> service.getById(99L, 5L));
    }

    // ---------- getByBooker ----------

    @Test
    void getByBooker_userNotExists_throwsNSE() {
        when(users.existsById(123L)).thenReturn(false);
        assertThrows(NoSuchElementException.class, () -> service.getByBooker(123L, BookingState.ALL));
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@ex.com");
        return u;
    }
}