package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.addBooking(userId, bookingDto);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping()
    public List<BookingDto> getBookingsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(required = false, defaultValue = "ALL")
                                                @Valid State state) {
        return bookingService.getBookingsByUserId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                 @RequestParam(required = false, defaultValue = "ALL")
                                                 @Valid State state) {
        return bookingService.getBookingsByOwnerId(ownerId, state);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable("bookingId") Long bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                     @RequestParam(required = false) Boolean approved) {
        return bookingService.approveBooking(bookingId, ownerId, approved);
    }
}
