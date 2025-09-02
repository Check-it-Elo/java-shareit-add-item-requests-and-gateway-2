package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class ItemRequestServiceImplIT {

    @Autowired private ItemRequestService service;
    @Autowired private ItemRequestRepository requestRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ItemRepository itemRepo;

    @Test
    void create_and_getById_returnsDtoWithCreatedAndEmptyItems() {
        // given: пользователь-инициатор
        User u = userRepo.save(user("Alice", "a@ex.com"));

        // when: создаём заявку через сервис
        ItemRequestCreateDto body = ItemRequestCreateDto.builder().description("Нужна дрель").build();
        ItemRequestDto created = service.create(u.getId(), body);

        // then: заявка создана
        assertNotNull(created.getId());
        assertEquals("Нужна дрель", created.getDescription());
        assertNotNull(created.getCreated());
        // подтягиваем через getById — должен вернуться тот же объект и без вещей
        ItemRequestDto byId = service.getById(u.getId(), created.getId());
        assertEquals(created.getId(), byId.getId());
        assertTrue(byId.getItems() == null || byId.getItems().isEmpty());
    }

    @Test
    void getAll_returnsOtherUsersRequests_descending_andPopulatesItems() {
        // given: три пользователя
        User viewer = userRepo.save(user("Viewer", "v@ex.com"));
        User requestor = userRepo.save(user("Bob", "b@ex.com"));
        User owner = userRepo.save(user("Owner", "o@ex.com"));

        // и две заявки другого пользователя (создадим напрямую, чтобы контролировать created)
        ItemRequest r1 = requestRepo.save(ItemRequest.builder()
                .description("Ищу стремянку")
                .requestor(requestor)
                .created(LocalDateTime.now().minusHours(2))
                .build());
        ItemRequest r2 = requestRepo.save(ItemRequest.builder()
                .description("Нужен перфоратор")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build());

        // и вещи, связанные с заявками (через FK request)
        itemRepo.save(item("Стремянка", owner, r1));
        itemRepo.save(item("Перфоратор Makita", owner, r2));

        List<ItemRequestDto> page = service.getAll(viewer.getId(), 0, 10);

        assertEquals(2, page.size());
        assertEquals(r2.getId(), page.get(0).getId()); // r2 новее r1
        assertEquals(r1.getId(), page.get(1).getId());

        assertNotNull(page.get(0).getItems());
        assertEquals(1, page.get(0).getItems().size());
        assertEquals("Перфоратор Makita", page.get(0).getItems().get(0).getName());

        assertNotNull(page.get(1).getItems());
        assertEquals(1, page.get(1).getItems().size());
        assertEquals("Стремянка", page.get(1).getItems().get(0).getName());
    }

    // --- helpers ---

    private static User user(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        return u;
    }

    private static Item item(String name, User owner, ItemRequest req) {
        Item i = new Item();
        i.setName(name);
        i.setDescription(name);
        i.setAvailable(true);
        i.setOwner(owner);
        i.setRequest(req);
        return i;
    }
}