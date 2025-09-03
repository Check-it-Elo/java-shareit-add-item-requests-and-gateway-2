package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingStateTest {

    @Test
    void from_null_returnsALL() {
        assertEquals(BookingState.ALL, BookingState.from(null));
    }

    @Test
    void from_valid_ignoreCase() {
        assertEquals(BookingState.CURRENT, BookingState.from("current"));
        assertEquals(BookingState.WAITING, BookingState.from("WaItInG"));
    }

    @Test
    void from_invalid_throwsIAE() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> BookingState.from("oops"));
        assertTrue(ex.getMessage().contains("Unknown state: oops"));
    }
}