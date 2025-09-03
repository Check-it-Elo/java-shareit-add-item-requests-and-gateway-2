package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean ItemService items;

    @Test
    void create_ok_returnsDto() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("Дрель").description("ok").available(true).build();
        when(items.create(eq(5L), any(ItemDto.class))).thenReturn(dto);

        mvc.perform(post("/items")
                        .header(HEADER, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ItemDto.builder()
                                .name("Дрель").description("ok").available(true).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOwnerItems_passesPaging() throws Exception {
        when(items.getOwnerItems(7L)).thenReturn(List.of());
        mvc.perform(get("/items").header(HEADER, 7L).param("from", "0").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        verify(items).getOwnerItems(7L);
    }

    @Test
    void missingHeader_returns400() throws Exception {
        mvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }
}