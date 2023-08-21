package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BookingServiceTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private User putUser(Long id) {
        return User.builder()
                .id(id)
                .name("Bill")
                .email("test@email.ru")
                .build();
    }

    private Item putItem(Long id) {
        return Item.builder()
                .id(id)
                .name("Отвёртка")
                .description("Крестовая отвёртка")
                .available(true)
                .build();
    }

    private Booking putBooking(Long id, User booker, Item item) {
        return Booking.builder()
                .id(id)
                .status(Status.WAITING)
                .booker(booker)
                .item(item)
                .build();
    }

    private final LocalDateTime time = LocalDateTime
            .of(2023, Month.AUGUST, 5, 5, 5, 5);

    @Test
    void addBookingTestCorrect() {
        User user = putUser(1L);
        Item item = putItem(2L);
        item.setOwner(user);
        LocalDateTime start = time;
        LocalDateTime end = time.plusHours(10);
        User booker = putUser(2L);
        Booking booking = putBooking(3L, booker, item);
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(item.getId())
                .build();
        when(itemRepository.findById(bookingDto.getItemId())).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.save(any())).thenReturn(booking);
        BookingDto actualBookingDto = bookingService.addBooking(booker.getId(), bookingDto);
        assertThat(actualBookingDto.getId(), equalTo(booking.getId()));
        assertThat(actualBookingDto.getStatus(), equalTo(booking.getStatus()));
        assertThat(actualBookingDto.getBooker().getId(), equalTo(booker.getId()));
        assertThat(actualBookingDto.getItem().getId(), equalTo(item.getId()));
        assertThat(actualBookingDto.getItem().getName(), equalTo(item.getName()));
        verify(itemRepository, times(1)).findById(bookingDto.getItemId());
        verify(userRepository, times(1)).findById(booker.getId());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void addBookingTestWhenItemIsNotFound() {
        User user = putUser(1L);
        Item item = putItem(2L);
        item.setOwner(user);
        LocalDateTime start = time;
        LocalDateTime end = time.plusHours(10);
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(item.getId())
                .build();
        when(itemRepository.findById(bookingDto.getItemId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.addBooking(user.getId(), bookingDto));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void addBookingTestWhenBookerIsOwner() {
        User user = putUser(1L);
        Item item = putItem(2L);
        item.setOwner(user);
        LocalDateTime start = time;
        LocalDateTime end = time.plusHours(10);
        Booking booking = putBooking(3L, user, item);
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(item.getId())
                .build();
        when(itemRepository.findById(bookingDto.getItemId())).thenReturn(Optional.of(item));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.save(any())).thenReturn(booking);
        assertThrows(NotFoundException.class, () -> bookingService.addBooking(user.getId(), bookingDto));
        verify(itemRepository, times(1)).findById(bookingDto.getItemId());
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void addBookingTestWhenItemIsNotAvailable() {
        User user = putUser(1L);
        Item item = Item.builder()
                .id(2L)
                .available(false)
                .build();
        item.setOwner(user);
        LocalDateTime start = time;
        LocalDateTime end = time.plusHours(10);
        User booker = putUser(2L);
        Booking booking = putBooking(3L, booker, item);
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(item.getId())
                .build();
        when(itemRepository.findById(bookingDto.getItemId())).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.save(any())).thenReturn(booking);
        assertThrows(ValidationException.class, () -> bookingService.addBooking(booker.getId(), bookingDto));
        verify(itemRepository, times(1)).findById(bookingDto.getItemId());
        verify(userRepository, times(1)).findById(booker.getId());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getBookingByIdTestCorrect() {
        User user = putUser(1L);
        Item item = putItem(2L);
        item.setOwner(user);
        User booker = putUser(2L);
        Booking booking = putBooking(3L, booker, item);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        BookingDto bookingDto = bookingService.getBookingById(user.getId(), booking.getId());
        assertThat(bookingDto.getId(), equalTo(booking.getId()));
        assertThat(bookingDto.getStatus(), equalTo(booking.getStatus()));
        assertThat(bookingDto.getBooker().getId(), equalTo(booker.getId()));
        assertThat(bookingDto.getItem().getId(), equalTo(item.getId()));
        verify(bookingRepository, times(1)).findById(bookingDto.getId());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void getBookingByIdTestWhenUserIsNotBooker() {
        User user = putUser(1L);
        User owner = putUser(2L);
        Item item = putItem(2L);
        item.setOwner(owner);
        User booker = putUser(3L);
        Booking booking = putBooking(3L, booker, item);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(user.getId(), booking.getId()));
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void getBookingByIdTestWhenUserIsNotFound() {
        User user = putUser(1L);
        Item item = putItem(2L);
        item.setOwner(user);
        User booker = putUser(2L);
        Booking booking = putBooking(3L, booker, item);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(user.getId(), booking.getId()));
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void getBookingsByUserIdTestWhenStateIsAll() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.ALL;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByUserId(booker.getId(), state, 0, 10);
        assertThat(booker.getId(), equalTo(bookingsDto.get(0).getBooker().getId()));
        verify(userRepository, times(1)).findById(booker.getId());
        verify(bookingRepository, times(1)).findByBookerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getBookingsByUserIdTestWhenStateIsCurrent() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.CURRENT;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(bookings);

        List<BookingDto> bookingsDto = bookingService.getBookingsByUserId(booker.getId(), state, 0, 10);
        assertThat(booker.getId(), equalTo(bookingsDto.get(0).getBooker().getId()));
        verify(userRepository, times(1)).findById(booker.getId());
        verify(bookingRepository, times(1))
                .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any());
    }

    @Test
    void getBookingsByUserIdTestWhenStateIsPast() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.PAST;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByUserId(booker.getId(), state, 0, 10);
        assertThat(booker.getId(), equalTo(bookingsDto.get(0).getBooker().getId()));
        verify(userRepository, times(1)).findById(booker.getId());
        verify(bookingRepository, times(1))
                .findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getBookingsByUserIdTestWhenStateIsFuture() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.FUTURE;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByUserId(booker.getId(), state, 0, 10);
        assertThat(booker.getId(), equalTo(bookingsDto.get(0).getBooker().getId()));
        verify(userRepository, times(1)).findById(booker.getId());
        verify(bookingRepository, times(1))
                .findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getBookingsByUserIdTestWhenStateIsWaiting() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.WAITING;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByUserId(booker.getId(), state, 0, 10);
        assertThat(booker.getId(), equalTo(bookingsDto.get(0).getBooker().getId()));
        verify(userRepository, times(1)).findById(booker.getId());
        verify(bookingRepository, times(1))
                .findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getBookingsByUserIdTestWhenStateIsRejected() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.REJECTED;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByUserId(booker.getId(), state, 0, 10);
        assertThat(booker.getId(), equalTo(bookingsDto.get(0).getBooker().getId()));
        verify(userRepository, times(1)).findById(booker.getId());
        verify(bookingRepository, times(1))
                .findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getBookingsByUserIdTestWhenUserIsNotFound() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.REJECTED;
        User booker = putUser(5L);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () ->
                bookingService.getBookingsByOwnerId(booker.getId(), state, 0, 10));
        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getBookingsByOwnerIdTestWhenStateIsAll() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.ALL;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository
                .findByItemOwnerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByOwnerId(owner.getId(), state, 0, 10);
        assertThat(item.getId(), equalTo(bookingsDto.get(0).getItem().getId()));
        verify(userRepository, times(1)).findById(owner.getId());
        verify(bookingRepository, times(1))
                .findByItemOwnerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getBookingsByOwnerIdTestWhenStateIsCurrent() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.CURRENT;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository
                .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByOwnerId(owner.getId(), state, 0, 10);
        assertThat(item.getId(), equalTo(bookingsDto.get(0).getItem().getId()));
        verify(userRepository, times(1)).findById(owner.getId());
        verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any());
    }

    @Test
    void getBookingsByOwnerIdTestWhenStateIsPast() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.PAST;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository
                .findByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByOwnerId(owner.getId(), state, 0, 10);
        assertThat(item.getId(), equalTo(bookingsDto.get(0).getItem().getId()));
        verify(userRepository, times(1)).findById(owner.getId());
        verify(bookingRepository, times(1))
                .findByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getBookingsByOwnerIdTestWhenStateIsFuture() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.FUTURE;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository
                .findByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByOwnerId(owner.getId(), state, 0, 10);
        assertThat(item.getId(), equalTo(bookingsDto.get(0).getItem().getId()));
        verify(userRepository, times(1)).findById(owner.getId());
        verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getBookingsByOwnerIdTestWhenStateIsWaiting() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.WAITING;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository
                .findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByOwnerId(owner.getId(), state, 0, 10);
        assertThat(item.getId(), equalTo(bookingsDto.get(0).getItem().getId()));
        verify(userRepository, times(1)).findById(owner.getId());
        verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getBookingsByOwnerIdTestWhenStateIsRejected() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.REJECTED;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository
                .findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        List<BookingDto> bookingsDto = bookingService.getBookingsByOwnerId(owner.getId(), state, 0, 10);
        assertThat(item.getId(), equalTo(bookingsDto.get(0).getItem().getId()));
        verify(userRepository, times(1)).findById(owner.getId());
        verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getBookingsByOwnerIdTestWhenUserIsNotFound() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        State state = State.ALL;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        List<Booking> bookings = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () ->
                bookingService.getBookingsByOwnerId(owner.getId(), state, 0, 10));
        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void approveBookingTestCorrect() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        Status status = Status.WAITING;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        booking.setStatus(status);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        BookingDto bookingDto = bookingService.approveBooking(booking.getId(), owner.getId(), true);
        assertThat(bookingDto.getId(), equalTo(booking.getId()));
        assertThat(bookingDto.getStatus(), equalTo(booking.getStatus()));
        assertThat(bookingDto.getBooker().getId(), equalTo(booker.getId()));
        assertThat(bookingDto.getItem().getId(), equalTo(item.getId()));
        assertThat(bookingDto.getItem().getName(), equalTo(item.getName()));
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void approveBookingTestWhenOwnerIsNotFound() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        Status status = Status.WAITING;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        booking.setStatus(status);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService
                .approveBooking(booking.getId(), owner.getId(), true));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBookingTestWhenBookingIsNotFound() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        Status status = Status.WAITING;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        booking.setStatus(status);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        assertThrows(NotFoundException.class, () -> bookingService
                .approveBooking(booking.getId(), owner.getId(), true));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBookingTestWhenBookerIsOwner() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        Status status = Status.WAITING;
        Booking booking = putBooking(1L, owner, item);
        booking.setStatus(status);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        assertThrows(NotFoundException.class, () -> bookingService
                .approveBooking(booking.getId(), owner.getId(), true));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBookingTestWhenUserIsNotOwner() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        User user = putUser(2L);
        Status status = Status.WAITING;
        Booking booking = putBooking(1L, owner, item);
        booking.setStatus(status);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        assertThrows(ValidationException.class, () -> bookingService
                .approveBooking(booking.getId(), user.getId(), true));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBookingTestWhenStatusIsApproved() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        Status status = Status.APPROVED;
        User booker = putUser(5L);
        Booking booking = putBooking(1L, booker, item);
        booking.setStatus(status);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        assertThrows(ValidationException.class, () -> bookingService
                .approveBooking(booking.getId(), owner.getId(), true));
        verify(bookingRepository, never()).save(any());
    }
}