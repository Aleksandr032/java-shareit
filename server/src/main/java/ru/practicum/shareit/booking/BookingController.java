package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody BookingDto bookingDto) {
        return bookingService.addBooking(userId, bookingDto);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(required = false, defaultValue = "ALL") State state,
                                                @RequestParam(required = false, defaultValue = "0") Integer from,
                                                @RequestParam(required = false, defaultValue = "10") Integer size) {
        return bookingService.getBookingsByUserId(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                 @RequestParam(required = false, defaultValue = "ALL") State state,
                                                 @RequestParam(required = false, defaultValue = "0") Integer from,
                                                 @RequestParam(required = false, defaultValue = "10") Integer size) {
        return bookingService.getBookingsByOwnerId(ownerId, state, from, size);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable("bookingId") Long bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                     @RequestParam(required = false) Boolean approved) {
        return bookingService.approveBooking(bookingId, ownerId, approved);
    }
}