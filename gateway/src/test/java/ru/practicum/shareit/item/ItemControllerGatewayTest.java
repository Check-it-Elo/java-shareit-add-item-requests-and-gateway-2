package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerGatewayTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    @DisplayName("POST /items без заголовка — 400")
    void create_missingHeader_returns400() throws Exception {
        String body = "{\"name\":\"Дрель\",\"description\":\"ok\",\"available\":true}";

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /items: пустое name — 400 (валидация @NotBlank)")
    void create_blankName_returns400() throws Exception {
        String body = "{\"name\":\"   \",\"description\":\"ok\",\"available\":true}";

        mvc.perform(post("/items")
                        .header(HEADER, 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /items — корректный запрос прокидывается в ItemClient")
    void getOwnerItems_ok_callsClient() throws Exception {
        when(itemClient.getOwnerItems(1L, 0, 3)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items")
                        .header(HEADER, 1)
                        .param("from", "0")
                        .param("size", "3"))
                .andExpect(status().isOk());

        verify(itemClient).getOwnerItems(1L, 0, 3);
    }

    @Test
    @DisplayName("POST /items — корректный запрос уходит в ItemClient")
    void create_ok_callsClient() throws Exception {
        when(itemClient.create(eq(5L), any())).thenReturn(ResponseEntity.ok().build());
        String body = "{\"name\":\"Дрель\",\"description\":\"ok\",\"available\":true}";

        mvc.perform(post("/items")
                        .header(HEADER, 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(itemClient).create(eq(5L), any());
    }

    @Test
    @DisplayName("POST /items/{id}/comment: пустой text — 400 (@NotBlank)")
    void addComment_blankText_returns400() throws Exception {
        String body = "{\"text\":\"   \"}";

        mvc.perform(post("/items/{itemId}/comment", 10)
                        .header(HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}