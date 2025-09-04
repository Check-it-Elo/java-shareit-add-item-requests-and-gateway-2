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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplPositiveTest {

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

    @Test
    void create_ok() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1)));
        Item it = new Item();
        it.setId(2L);
        it.setOwner(user(9));
        it.setAvailable(true);
        when(items.findById(2L)).thenReturn(Optional.of(it));
        when(bookings.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(77L);
            return b;
        });

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        var out = service.create(1L, dto);
        assertEquals(77L, out.getId());
        assertEquals(BookingStatus.WAITING, out.getStatus());
    }

    @Test
    void getById_ownerOrBooker_ok() {
        Booking b = new Booking();
        b.setId(5L);
        b.setBooker(user(7));
        Item it = new Item();
        it.setOwner(user(3));
        b.setItem(it);
        when(bookings.findById(5L)).thenReturn(Optional.of(b));

        assertEquals(5L, service.getById(7L, 5L).getId()); // booker
        assertEquals(5L, service.getById(3L, 5L).getId()); // owner
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@ex.com");
        return u;
    }
}