package ru.practicum.shareit.booking;

public enum BookingStatus {
    WAITING,   // новое, ждёт одобрения владельца
    APPROVED,  // подтверждено владельцем
    REJECTED,  // отклонено владельцем
    CANCELED   // отменено создателем брони
}