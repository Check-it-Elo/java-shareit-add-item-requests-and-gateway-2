package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock BookingRepository bookings;
    @Mock UserRepository users;
    @Mock ItemRepository items;

    BookingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BookingServiceImpl(bookings, users, items);
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id+ "@ex.com");
        return u;
    }

    private static Item item(long id, long ownerId, boolean available) {
        User owner = user(ownerId);
        Item it = new Item();
        it.setId(id);
        it.setName("Item" + id);
        it.setOwner(owner);
        it.setAvailable(available);
        return it;
    }

    @Test
    void create_valid_savesWAITING_andReturnsDto() {
        long userId = 1L;
        when(users.findById(userId)).thenReturn(Optional.of(user(userId)));
        when(items.findById(10L)).thenReturn(Optional.of(item(10L, 2L, true)));

        Booking saved = new Booking();
        saved.setId(100L);
        saved.setStart(LocalDateTime.now().plusHours(1));
        saved.setEnd(LocalDateTime.now().plusHours(2));
        saved.setBooker(user(userId));
        saved.setItem(item(10L, 2L, true));
        saved.setStatus(BookingStatus.WAITING);

        when(bookings.save(any(Booking.class))).thenReturn(saved);

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        BookingDto out = service.create(userId, dto);

        // проверяем, что сохранили WAITING
        ArgumentCaptor<Booking> cap = ArgumentCaptor.forClass(Booking.class);
        verify(bookings).save(cap.capture());
        assertEquals(BookingStatus.WAITING, cap.getValue().getStatus());

        // и что вернулся заполненный DTO
        assertEquals(100L, out.getId());
        assertEquals(10L, out.getItem().getId());
        assertEquals("Item10", out.getItem().getName());
        assertEquals(userId, out.getBooker().getId());
        assertEquals(BookingStatus.WAITING, out.getStatus());
    }

    @Test
    void create_nullBody_throwsIAE() {
        assertThrows(IllegalArgumentException.class, () -> service.create(1L, null));
    }

    @Test
    void create_missingItem_throwsNoSuchElement() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(items.findById(10L)).thenReturn(Optional.empty());

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(NoSuchElementException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_itemNotAvailable_throwsIAE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(items.findById(10L)).thenReturn(Optional.of(item(10L, 2L, false)));

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_ownerBooksOwnItem_throwsNoSuchElement() {
        long ownerId = 5L;
        when(users.findById(ownerId)).thenReturn(Optional.of(user(ownerId)));
        when(items.findById(10L)).thenReturn(Optional.of(item(10L, ownerId, true)));

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(NoSuchElementException.class, () -> service.create(ownerId, dto));
    }

    @Test
    void approve_ownerApproves_waitingToApproved() {
        Booking b = new Booking();
        b.setId(1L);
        b.setItem(item(10L, 2L, true));
        b.setBooker(user(3L));
        b.setStatus(BookingStatus.WAITING);

        when(bookings.findById(1L)).thenReturn(Optional.of(b));

        BookingDto dto = service.approve(2L, 1L, true);
        assertEquals(BookingStatus.APPROVED, dto.getStatus());
    }

    @Test
    void approve_wrongOwner_throwsSecurity() {
        Booking b = new Booking();
        b.setId(1L);
        b.setItem(item(10L, 2L, true));
        b.setBooker(user(3L));
        b.setStatus(BookingStatus.WAITING);

        when(bookings.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(SecurityException.class, () -> service.approve(99L, 1L, true));
    }

    @Test
    void approve_notWaiting_throwsIllegalState() {
        Booking b = new Booking();
        b.setId(1L);
        b.setItem(item(10L, 2L, true));
        b.setBooker(user(3L));
        b.setStatus(BookingStatus.APPROVED);

        when(bookings.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(IllegalStateException.class, () -> service.approve(2L, 1L, false));
    }

    @Test
    void getById_forBooker_ok() {
        Booking b = new Booking();
        b.setId(1L);
        b.setItem(item(10L, 2L, true));
        b.setBooker(user(3L));
        b.setStatus(BookingStatus.WAITING);
        when(bookings.findById(1L)).thenReturn(Optional.of(b));

        BookingDto dto = service.getById(3L, 1L);
        assertEquals(1L, dto.getId());
    }

    @Test
    void getById_stranger_throwsSecurity() {
        Booking b = new Booking();
        b.setId(1L);
        b.setItem(item(10L, 2L, true));
        b.setBooker(user(3L));
        when(bookings.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(SecurityException.class, () -> service.getById(9L, 1L));
    }

    @Test
    void getByBooker_switchBranches_callProperRepos() {
        when(users.existsById(1L)).thenReturn(true);
        when(bookings.findByBooker_Id(eq(1L), any(Sort.class))).thenReturn(List.of());
        when(bookings.findCurrentByBooker(eq(1L), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of());
        when(bookings.findByBooker_IdAndEndBefore(eq(1L), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of());
        when(bookings.findByBooker_IdAndStartAfter(eq(1L), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of());
        when(bookings.findByBooker_IdAndStatus(eq(1L), eq(BookingStatus.WAITING), any(Sort.class))).thenReturn(List.of());
        when(bookings.findByBooker_IdAndStatus(eq(1L), eq(BookingStatus.REJECTED), any(Sort.class))).thenReturn(List.of());

        service.getByBooker(1L, BookingState.ALL);
        service.getByBooker(1L, BookingState.CURRENT);
        service.getByBooker(1L, BookingState.PAST);
        service.getByBooker(1L, BookingState.FUTURE);
        service.getByBooker(1L, BookingState.WAITING);
        service.getByBooker(1L, BookingState.REJECTED);

        verify(bookings).findByBooker_Id(eq(1L), any(Sort.class));
        verify(bookings).findCurrentByBooker(eq(1L), any(LocalDateTime.class), any(Sort.class));
        verify(bookings).findByBooker_IdAndEndBefore(eq(1L), any(LocalDateTime.class), any(Sort.class));
        verify(bookings).findByBooker_IdAndStartAfter(eq(1L), any(LocalDateTime.class), any(Sort.class));
        verify(bookings).findByBooker_IdAndStatus(eq(1L), eq(BookingStatus.WAITING), any(Sort.class));
        verify(bookings).findByBooker_IdAndStatus(eq(1L), eq(BookingStatus.REJECTED), any(Sort.class));
    }

    @Test
    void getByOwner_switchBranches_callProperRepos() {
        when(users.existsById(2L)).thenReturn(true);
        when(bookings.findByOwner(eq(2L), any(Sort.class))).thenReturn(List.of());
        when(bookings.findCurrentByOwner(eq(2L), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of());
        when(bookings.findPastByOwner(eq(2L), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of());
        when(bookings.findFutureByOwner(eq(2L), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of());
        when(bookings.findByOwnerAndStatus(eq(2L), eq(BookingStatus.WAITING), any(Sort.class))).thenReturn(List.of());
        when(bookings.findByOwnerAndStatus(eq(2L), eq(BookingStatus.REJECTED), any(Sort.class))).thenReturn(List.of());

        service.getByOwner(2L, BookingState.ALL);
        service.getByOwner(2L, BookingState.CURRENT);
        service.getByOwner(2L, BookingState.PAST);
        service.getByOwner(2L, BookingState.FUTURE);
        service.getByOwner(2L, BookingState.WAITING);
        service.getByOwner(2L, BookingState.REJECTED);

        verify(bookings).findByOwner(eq(2L), any(Sort.class));
        verify(bookings).findCurrentByOwner(eq(2L), any(LocalDateTime.class), any(Sort.class));
        verify(bookings).findPastByOwner(eq(2L), any(LocalDateTime.class), any(Sort.class));
        verify(bookings).findFutureByOwner(eq(2L), any(LocalDateTime.class), any(Sort.class));
        verify(bookings).findByOwnerAndStatus(eq(2L), eq(BookingStatus.WAITING), any(Sort.class));
        verify(bookings).findByOwnerAndStatus(eq(2L), eq(BookingStatus.REJECTED), any(Sort.class));
    }
}