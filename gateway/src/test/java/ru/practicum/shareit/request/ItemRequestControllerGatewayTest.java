package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerGatewayTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;

    // мок клиента, через него gateway прокидывает дальше
    @MockBean ItemRequestClient client;

    @Test
    @DisplayName("GET /requests/all с size=0 — 400 (валидация параметров)")
    void getAll_invalidSize_returnsBadRequest() throws Exception {
        mvc.perform(get("/requests/all")
                        .header(HEADER, "1")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("POST /requests без заголовка пользователя — 400")
    void create_missingHeader_returnsBadRequest() throws Exception {
        String body = """
                {"description": "Нужна дрель"}
                """;

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }
}