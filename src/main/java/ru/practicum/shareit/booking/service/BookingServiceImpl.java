package ru.practicum.shareit.booking.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotImplementedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    @Override
    public BookingDto addBooking(Long userId, BookingDto bookingDto) {
        User user = checkUserById(userId);
        Item item = checkItemId(bookingDto.getItemId());
        if (userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Вы являетесь владельцем данного предмета");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Предмет не доступен для бронирвоания");
        }
        Booking booking = bookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        checkUserById(userId);
        Booking booking = checkBookingById(bookingId);
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь id = " + userId +
                    " не имеет прав на предмет с id = " + bookingId);
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByUserId(Long userId, State state) {
        User user = checkUserById(userId);
        switch (state) {
            case ALL:
                return bookingRepository.findByBookerIdOrderByStartDesc(userId).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                userId, LocalDateTime.now(), LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId,
                                LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId,
                                LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId,
                                Status.WAITING).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId,
                                Status.REJECTED).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            default:
                throw new NotImplementedException("Неизвестное значение параметра state = " + state);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByOwnerId(Long ownerId, State state) {
        User owner = checkUserById(ownerId);
        switch (state) {
            case ALL:
                return bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                ownerId, LocalDateTime.now(), LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId,
                                LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId,
                                LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(ownerId,
                                Status.WAITING).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(ownerId,
                                Status.REJECTED).stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            default:
                throw new NotImplementedException("Неизвестное значение параметра state = " + state);
        }
    }

    @Transactional
    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, Boolean approved) {
        checkUserById(ownerId);
        Booking booking = checkBookingById(bookingId);
        if (booking.getBooker().getId().equals(ownerId)) {
            throw new NotFoundException("Нельзя изменить статус своей заявки");
        }
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ValidationException("Ошибка доступа. Только владелец может вносить изменения");
        }

        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new ValidationException("Бронирование с id = " + bookingId + " уже подтверждено");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    private User checkUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь c id = " + userId + " не найден"));
    }

    private Item checkItemId(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Предмет с id = " + itemId + " не найден"));
    }

    private Booking checkBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Бронирование с id = " + bookingId + " не найдена"));
    }
}
