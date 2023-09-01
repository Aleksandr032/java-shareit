package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class BookingController {
    private final BookingClient bookingClient;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(USER_ID) Long userId,
                                             @Valid @RequestBody BookingDto bookingDto) {
        log.info("Создание бронирования {}, userId={}", bookingDto, userId);
        return bookingClient.addBooking(userId, bookingDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(USER_ID) Long userId,
                                                 @PathVariable("bookingId") Long bookingId) {
        log.info("Получение бронирования {}, userId={}", bookingId, userId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByUserId(@RequestHeader(USER_ID) Long userId,
                                                      @RequestParam(defaultValue = "ALL")
                                                      @Valid State state,
                                                      @RequestParam(required = false, defaultValue = "0")
                                                      @PositiveOrZero Integer from,
                                                      @RequestParam(required = false, defaultValue = "10")
                                                      @Positive Integer size) {
        log.info("Получение бронирования со state {}, userId={}", state, userId);
        return bookingClient.getBookingsByUserId(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwnerId(@RequestHeader(USER_ID) Long ownerId,
                                                       @RequestParam(defaultValue = "ALL")
                                                       @Valid State state,
                                                       @RequestParam(required = false, defaultValue = "0")
                                                       @PositiveOrZero Integer from,
                                                       @RequestParam(required = false, defaultValue = "10")
                                                       @Positive Integer size) {
        log.info("Получение бронирования со state {}, ownerId={}", state, ownerId);
        return bookingClient.getBookingsByOwnerId(ownerId, state, from, size);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@PathVariable("bookingId") Long bookingId,
                                                 @RequestHeader(USER_ID) Long ownerId,
                                                 @RequestParam(required = false) Boolean approved) {
        log.info("Присвоение статуса для bookingId={}, ownerId={}", bookingId, ownerId);
        return bookingClient.approveBooking(bookingId, ownerId, approved);
    }
}