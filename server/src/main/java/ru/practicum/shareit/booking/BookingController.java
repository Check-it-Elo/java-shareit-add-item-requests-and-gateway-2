package ru.practicum.shareit.booking;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookings;

    public BookingController(BookingService bookings) {
        this.bookings = bookings;
    }

    @PostMapping
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @RequestBody BookingCreateDto body) {
        return bookings.create(userId, body);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @PathVariable Long bookingId,
                              @RequestParam boolean approved) {
        return bookings.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long bookingId) {
        return bookings.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> myBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @RequestParam(name = "state", required = false) String state) {
        return bookings.getByBooker(userId, BookingState.from(state));
    }

    @GetMapping("/owner")
    public List<BookingDto> ownerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                          @RequestParam(name = "state", required = false) String state) {
        return bookings.getByOwner(ownerId, BookingState.from(state));
    }

}