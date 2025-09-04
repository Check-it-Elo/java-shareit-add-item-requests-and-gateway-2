package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerGatewayTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    @DisplayName("POST /bookings без заголовка — 400")
    void create_missingHeader_returns400() throws Exception {
        String body = "{\"itemId\":10,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-01T12:00:00\"}";

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /bookings: start в прошлом — 400 (валидация @FutureOrPresent)")
    void create_pastStart_returns400() throws Exception {
        String body = "{\"itemId\":10,\"start\":\"2000-01-01T10:00:00\",\"end\":\"2030-01-01T12:00:00\"}";

        mvc.perform(post("/bookings")
                        .header(HEADER, 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /bookings/{id}?approved=true — прокидывает флаг в BookingClient")
    void approve_ok() throws Exception {
        when(bookingClient.approve(2L, 99L, true)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(patch("/bookings/{bookingId}", 99)
                        .header(HEADER, 2)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).approve(2L, 99L, true);
    }

    @Test
    @DisplayName("GET /bookings — прокидывает параметры state/from/size")
    void getBookings_ok() throws Exception {
        when(bookingClient.getBookings(eq(7L), eq("current"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/bookings")
                        .header(HEADER, 7)
                        .param("state", "current")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(bookingClient).getBookings(7L, "current", 0, 10);
    }

    @Test
    @DisplayName("GET /bookings/owner — прокидывает параметры state/from/size")
    void getOwnerBookings_ok() throws Exception {
        when(bookingClient.getOwnerBookings(eq(3L), eq("ALL"), eq(5), eq(2)))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/bookings/owner")
                        .header(HEADER, 3)
                        .param("state", "ALL")
                        .param("from", "5")
                        .param("size", "2"))
                .andExpect(status().isOk());

        verify(bookingClient).getOwnerBookings(3L, "ALL", 5, 2);
    }
}