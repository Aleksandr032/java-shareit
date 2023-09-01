package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(Long userId, BookingDto bookingDto);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getBookingsByUserId(Long userId, State state, Integer from, Integer size);

    List<BookingDto> getBookingsByOwnerId(Long ownerId, State state, Integer from, Integer size);

    BookingDto approveBooking(Long bookingId, Long ownerId, Boolean approved);
}
