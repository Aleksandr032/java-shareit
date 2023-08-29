package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.State;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @Valid @RequestBody BookingDto bookingDto) {
        return bookingClient.addBooking(userId, bookingDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable("bookingId") Long bookingId) {
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam(required = false, defaultValue = "ALL")
                                                      @Valid State state,
                                                      @RequestParam(required = false, defaultValue = "0")
                                                      @PositiveOrZero Integer from,
                                                      @RequestParam(required = false, defaultValue = "10")
                                                      @Positive Integer size) {
        return bookingClient.getBookingsByUserId(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                       @RequestParam(required = false, defaultValue = "ALL")
                                                       @Valid State state,
                                                       @RequestParam(required = false, defaultValue = "0")
                                                       @PositiveOrZero Integer from,
                                                       @RequestParam(required = false, defaultValue = "10")
                                                       @Positive Integer size) {
        return bookingClient.getBookingsByOwnerId(ownerId, state, from, size);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@PathVariable("bookingId") Long bookingId,
                                                 @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                 @RequestParam(required = false) Boolean approved) {
        return bookingClient.approveBooking(bookingId, ownerId, approved);
    }
}