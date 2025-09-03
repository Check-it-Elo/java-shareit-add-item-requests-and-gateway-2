package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplPositiveTest {

    private ItemRepository items;
    private UserRepository users;
    private CommentRepository comments;
    private BookingRepository bookings;
    private ItemRequestRepository requests;
    private ItemServiceImpl service;

    @BeforeEach
    void setUp() {
        items = mock(ItemRepository.class);
        users = mock(UserRepository.class);
        comments = mock(CommentRepository.class);
        bookings = mock(BookingRepository.class);
        requests = mock(ItemRequestRepository.class);
        service = new ItemServiceImpl(items, users, comments, bookings, requests);
    }

    @Test
    void create_withRequest_ok() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1)));
        when(requests.findById(100L)).thenReturn(Optional.of(new ItemRequest()));
        when(items.save(any(Item.class))).thenAnswer(inv -> {
            Item it = inv.getArgument(0);
            it.setId(50L);
            return it;
        });

        ItemDto in = ItemDto.builder().name("N").description("D").available(true).requestId(100L).build();
        ItemDto out = service.create(1L, in);

        assertEquals(50L, out.getId());
        assertEquals("N", out.getName());
    }

    @Test
    void addComment_ok() {
        Item item = new Item();
        item.setId(2L);
        item.setOwner(user(9));
        when(items.findById(2L)).thenReturn(Optional.of(item));
        when(users.findById(1L)).thenReturn(Optional.of(user(1)));
        when(bookings.existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                eq(1L), eq(2L), any(LocalDateTime.class), eq(BookingStatus.APPROVED))).thenReturn(true);
        when(comments.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(30L);
            return c;
        });

        CommentDto out = service.addComment(1L, 2L, CommentCreateDto.builder().text("ok").build());
        assertEquals(30L, out.getId());
        assertEquals("ok", out.getText());
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@ex.com");
        return u;
    }
}