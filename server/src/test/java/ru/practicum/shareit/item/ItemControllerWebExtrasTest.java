package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerWebExtrasTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @Test
    @DisplayName("PATCH /items/{id} — update прокидывает userId, itemId и body")
    void update_ok() throws Exception {
        ItemDto patch = ItemDto.builder().name("NewName").build();
        ItemDto resp = ItemDto.builder().id(10L).name("NewName").description("d").available(true).build();

        when(itemService.update(eq(5L), eq(10L), any(ItemDto.class))).thenReturn(resp);

        mvc.perform(patch("/items/{id}", 10)
                        .header(HEADER, 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("NewName"));

        verify(itemService).update(eq(5L), eq(10L), any(ItemDto.class));
    }

    @Test
    @DisplayName("GET /items/{id} — getById прокидывает заголовок")
    void getById_ok() throws Exception {
        ItemDto resp = ItemDto.builder().id(7L).name("N").description("D").available(true).build();
        when(itemService.getById(3L, 7L)).thenReturn(resp);

        mvc.perform(get("/items/{id}", 7).header(HEADER, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(itemService).getById(3L, 7L);
    }

    @Test
    @DisplayName("GET /items/search?text=drill — search прокидывает text")
    void search_ok() throws Exception {
        when(itemService.search("drill")).thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .header(HEADER, 1)
                        .param("text", "drill"))
                .andExpect(status().isOk());

        verify(itemService).search("drill");
    }

    @Test
    @DisplayName("POST /items/{id}/comment — addComment")
    void addComment_ok() throws Exception {
        CommentCreateDto body = CommentCreateDto.builder().text("ok").build();
        CommentDto resp = CommentDto.builder().id(1L).text("ok").build();
        when(itemService.addComment(eq(2L), eq(11L), any(CommentCreateDto.class))).thenReturn(resp);

        mvc.perform(post("/items/{itemId}/comment", 11)
                        .header(HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(itemService).addComment(eq(2L), eq(11L), any(CommentCreateDto.class));
    }
}