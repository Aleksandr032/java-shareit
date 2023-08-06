package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(Long userId, BookingDto bookingDto);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getBookingsByUserId(Long userId, State state);

    List<BookingDto> getBookingsByOwnerId(Long ownerId, State state);

    BookingDto approveBooking(Long bookingId, Long ownerId, Boolean approved);
}
