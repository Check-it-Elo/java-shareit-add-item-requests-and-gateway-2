package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@ActiveProfiles("test")
class ItemRequestControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean ItemRequestService itemRequestService;

    @Test
    @DisplayName("POST /requests — создаёт заявку и возвращает DTO")
    void create_returnsOkAndBody() throws Exception {
        long userId = 1L;
        ItemRequestCreateDto body = ItemRequestCreateDto.builder()
                .description("Нужна дрель")
                .build();

        ItemRequestDto resp = new ItemRequestDto();
        resp.setId(10L);
        resp.setDescription("Нужна дрель");
        resp.setCreated(LocalDateTime.now());
        resp.setItems(List.of());

        when(itemRequestService.create(eq(userId), any(ItemRequestCreateDto.class)))
                .thenReturn(resp);

        mvc.perform(post("/requests")
                        .header(HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description", is("Нужна дрель")))
                .andExpect(jsonPath("$.items", hasSize(0)));

        verify(itemRequestService).create(eq(userId), any(ItemRequestCreateDto.class));
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    @DisplayName("GET /requests — возвращает свои заявки с вложенными вещами")
    void getOwn_returnsList() throws Exception {
        long userId = 2L;

        ItemDto item1 = new ItemDto();
        item1.setId(100L);
        item1.setName("Перфоратор");

        ItemRequestDto r1 = new ItemRequestDto();
        r1.setId(11L);
        r1.setDescription("Ищу перфоратор");
        r1.setCreated(LocalDateTime.now().minusHours(1));
        r1.setItems(List.of(item1));

        when(itemRequestService.getOwn(userId)).thenReturn(List.of(r1));

        mvc.perform(get("/requests").header(HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id").value(100))
                .andExpect(jsonPath("$[0].items[0].name", is("Перфоратор")));

        verify(itemRequestService).getOwn(userId);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    @DisplayName("GET /requests/all?from&size — отдаёт чужие заявки постранично")
    void getAll_returnsPageAndPassesPaging() throws Exception {
        long userId = 3L;
        int from = 5, size = 2;

        ItemRequestDto r = new ItemRequestDto();
        r.setId(22L);
        r.setDescription("Нужна стремянка");
        r.setCreated(LocalDateTime.now());
        r.setItems(List.of());

        when(itemRequestService.getAll(userId, from, size)).thenReturn(List.of(r));

        mvc.perform(get("/requests/all")
                        .header(HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(22));

        verify(itemRequestService).getAll(userId, from, size);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    @DisplayName("GET /requests/{id} — отдаёт одну заявку по id")
    void getById_returnsOne() throws Exception {
        long userId = 4L;
        long requestId = 77L;

        ItemRequestDto r = new ItemRequestDto();
        r.setId(requestId);
        r.setDescription("Нужна бензопила");
        r.setCreated(LocalDateTime.now());
        r.setItems(List.of());

        when(itemRequestService.getById(userId, requestId)).thenReturn(r);

        mvc.perform(get("/requests/{id}", requestId).header(HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.description", is("Нужна бензопила")));

        verify(itemRequestService).getById(userId, requestId);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    @DisplayName("Валидация отсутствующего заголовка X-Sharer-User-Id — 400")
    void missingHeader_returnsBadRequest() throws Exception {
        mvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(itemRequestService);
    }
}