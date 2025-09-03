package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ItemServiceImplIT {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("getOwnerItems: заполняет lastBooking/nextBooking и comments для вещей владельца")
    void getOwnerItems_populatesLastNextAndComments() {
        // users
        User owner = saveUser("Owner", "owner@ex.com");
        User booker = saveUser("Booker", "booker@ex.com");

        // items
        Item drill = saveItem("Дрель", "с ударом", true, owner);
        Item ladder = saveItem("Лестница", "2м", true, owner);

        // comments (к одной из вещей)
        saveComment(drill, booker, "норм");

        // bookings: для drill — одна прошедшая (last) и одна будущая (next), обе APPROVED
        LocalDateTime now = LocalDateTime.now();
        saveBooking(drill, booker, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);
        saveBooking(drill, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);

        // для ladder бронирований нет — last/next должны быть null, comments пуст
        List<ItemDto> dtos = itemService.getOwnerItems(owner.getId());

        // проверяем, что в списке есть обе вещи
        assertThat(dtos).hasSize(2);

        ItemDto drillDto = dtos.stream().filter(d -> d.getId().equals(drill.getId())).findFirst().orElseThrow();
        ItemDto ladderDto = dtos.stream().filter(d -> d.getId().equals(ladder.getId())).findFirst().orElseThrow();

        // drill: есть комментарий, проставлены lastBooking и nextBooking
        assertThat(drillDto.getComments()).hasSize(1);
        assertThat(drillDto.getLastBooking()).isNotNull();
        assertThat(drillDto.getNextBooking()).isNotNull();

        // ladder: без бронирований и комментариев
        assertThat(ladderDto.getComments()).isEmpty();
        assertThat(ladderDto.getLastBooking()).isNull();
        assertThat(ladderDto.getNextBooking()).isNull();
    }

    @Test
    @DisplayName("getOwnerItems: у пользователя без вещей — пустой список")
    void getOwnerItems_noItems_returnsEmpty() {
        User lonelyOwner = saveUser("Lonely", "lonely@ex.com");
        List<ItemDto> dtos = itemService.getOwnerItems(lonelyOwner.getId());
        assertThat(dtos).isEmpty();
    }

    private User saveUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        return userRepository.save(u);
    }

    private Item saveItem(String name, String description, boolean available, User owner) {
        Item it = new Item();
        it.setName(name);
        it.setDescription(description);
        it.setAvailable(available);
        it.setOwner(owner);
        return itemRepository.save(it);
    }

    private void saveComment(Item item, User author, String text) {
        Comment c = Comment.builder()
                .text(text)
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        commentRepository.save(c);
    }

    private void saveBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        Booking b = new Booking();
        b.setItem(item);
        b.setBooker(booker);
        b.setStart(start);
        b.setEnd(end);
        b.setStatus(status);
        bookingRepository.save(b);
    }
}