package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookings;
    private final UserRepository users;
    private final ItemRepository items;

    private static final Sort DESC_BY_START = Sort.by(Sort.Direction.DESC, "start");

    public BookingServiceImpl(BookingRepository bookings, UserRepository users, ItemRepository items) {
        this.bookings = bookings;
        this.users = users;
        this.items = items;
    }

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingCreateDto dto) {
        if (dto == null) throw new IllegalArgumentException("Body must not be null");
        if (dto.getItemId() == null) throw new IllegalArgumentException("itemId is required");
        if (dto.getStart() == null || dto.getEnd() == null) throw new IllegalArgumentException("start/end required");
        if (!dto.getStart().isBefore(dto.getEnd())) throw new IllegalArgumentException("start must be before end");

        User booker = users.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        Item item = items.findById(dto.getItemId()).orElseThrow(() -> new NoSuchElementException("Item not found: " + dto.getItemId()));

        if (!item.isAvailable()) throw new IllegalArgumentException("Item is not available for booking");
        if (item.getOwner().getId().equals(userId)) throw new NoSuchElementException("Owner cannot book own item");

        Booking b = new Booking();
        b.setItem(item);
        b.setBooker(booker);
        b.setStart(dto.getStart());
        b.setEnd(dto.getEnd());
        b.setStatus(BookingStatus.WAITING);
        b = bookings.save(b);

        return toDto(b);
    }

    @Override
    @Transactional
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking b = bookings.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + bookingId));
        if (!b.getItem().getOwner().getId().equals(ownerId)) {
            throw new SecurityException("Only the item owner can approve/reject");
        }
        if (b.getStatus() != BookingStatus.WAITING) {
            throw new IllegalStateException("Booking is already decided");
        }
        b.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return toDto(b);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking b = bookings.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + bookingId));
        Long ownerId = b.getItem().getOwner().getId();
        if (!b.getBooker().getId().equals(userId) && !ownerId.equals(userId)) {
            throw new SecurityException("Only booker or item owner can view this booking");
        }
        return toDto(b);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId, BookingState state) {

        if (!users.existsById(userId)) {
            throw new NoSuchElementException("User not found: " + userId);
        }

        LocalDateTime now = LocalDateTime.now();

        List<Booking> list;
        switch (state) {
            case ALL:
                list = bookings.findByBooker_Id(userId, DESC_BY_START);
                break;
            case CURRENT:
                list = bookings.findCurrentByBooker(userId, now, DESC_BY_START);
                break;
            case PAST:
                list = bookings.findByBooker_IdAndEndBefore(userId, now, DESC_BY_START);
                break;
            case FUTURE:
                list = bookings.findByBooker_IdAndStartAfter(userId, now, DESC_BY_START);
                break;
            case WAITING:
                list = bookings.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, DESC_BY_START);
                break;
            case REJECTED:
                list = bookings.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, DESC_BY_START);
                break;
            default:
                list = List.of();
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getByOwner(Long ownerId, BookingState state) {

        if (!users.existsById(ownerId)) {
            throw new NoSuchElementException("User not found: " + ownerId);
        }

        LocalDateTime now = LocalDateTime.now();

        List<Booking> list;
        switch (state) {
            case ALL:
                list = bookings.findByOwner(ownerId, DESC_BY_START);
                break;
            case CURRENT:
                list = bookings.findCurrentByOwner(ownerId, now, DESC_BY_START);
                break;
            case PAST:
                list = bookings.findPastByOwner(ownerId, now, DESC_BY_START);
                break;
            case FUTURE:
                list = bookings.findFutureByOwner(ownerId, now, DESC_BY_START);
                break;
            case WAITING:
                list = bookings.findByOwnerAndStatus(ownerId, BookingStatus.WAITING, DESC_BY_START);
                break;
            case REJECTED:
                list = bookings.findByOwnerAndStatus(ownerId, BookingStatus.REJECTED, DESC_BY_START);
                break;
            default:
                list = List.of();
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    // mapper
    private BookingDto toDto(Booking b) {
        return BookingDto.builder()
                .id(b.getId())
                .start(b.getStart())
                .end(b.getEnd())
                .status(b.getStatus())
                .booker(BookingDto.UserShortDto.builder()
                        .id(b.getBooker().getId())
                        .build())
                .item(BookingDto.ItemShortDto.builder()
                        .id(b.getItem().getId())
                        .name(b.getItem().getName())
                        .build())
                .build();
    }

}