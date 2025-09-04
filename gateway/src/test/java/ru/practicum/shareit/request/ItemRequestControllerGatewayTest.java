package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerGatewayTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestClient client;

    @Test
    @DisplayName("POST /requests без заголовка — 400")
    void create_missingHeader_returns400() throws Exception {
        String body = "{\"description\":\"Нужна дрель\"}";

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /requests: пустой description — 400 (@NotBlank)")
    void create_blankDescription_returns400() throws Exception {
        String body = "{\"description\":\"   \"}";

        mvc.perform(post("/requests")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests — прокидывает userId в клиент")
    void getOwn_ok_callsClient() throws Exception {
        when(client.getOwn(1L)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/requests").header(HEADER, 1))
                .andExpect(status().isOk());

        verify(client).getOwn(1L);
    }


    @Test
    @DisplayName("GET /requests/{id} — прокидывает userId в клиент")
    void getById_ok_callsClient() throws Exception {
        when(client.getById(1L, 77L)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/requests/{requestId}", 77).header(HEADER, 1))
                .andExpect(status().isOk());

        verify(client).getById(1L, 77L);
    }
}