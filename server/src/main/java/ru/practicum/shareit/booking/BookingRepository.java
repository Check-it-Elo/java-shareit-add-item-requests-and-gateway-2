package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //списки для "как бронировал" (booker)
    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    @Query("select b from Booking b where b.booker.id = ?1 and ?2 between b.start and b.end")
    List<Booking> findCurrentByBooker(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBooker_IdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_IdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBooker_IdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    //списки для "как владелец вещей" (owner)
    @Query("select b from Booking b where b.item.owner.id = ?1")
    List<Booking> findByOwner(Long ownerId, Sort sort);

    @Query("select b from Booking b where b.item.owner.id = ?1 and ?2 between b.start and b.end")
    List<Booking> findCurrentByOwner(Long ownerId, LocalDateTime now, Sort sort);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.end < ?2")
    List<Booking> findPastByOwner(Long ownerId, LocalDateTime now, Sort sort);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start > ?2")
    List<Booking> findFutureByOwner(Long ownerId, LocalDateTime now, Sort sort);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.status = ?2")
    List<Booking> findByOwnerAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findTop1ByItem_IdAndStartBeforeOrderByStartDesc(Long itemId, LocalDateTime now);

    List<Booking> findTop1ByItem_IdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime now);

    // last/next ТОЛЬКО по APPROVED
    Optional<Booking> findTop1ByItem_IdAndStartBeforeAndStatusOrderByStartDesc(
            Long itemId, LocalDateTime now, BookingStatus status);

    Optional<Booking> findTop1ByItem_IdAndStartAfterAndStatusOrderByStartAsc(
            Long itemId, LocalDateTime now, BookingStatus status);

    // право комментировать: было ЗАВЕРШЁННОЕ APPROVED-бронирование этой вещи
    boolean existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
            Long userId, Long itemId, LocalDateTime before, BookingStatus status);

}