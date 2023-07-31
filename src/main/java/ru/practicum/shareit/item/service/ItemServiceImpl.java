package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ErrorAccess;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final BookingMapper bookingMapper;


    @Transactional
    @Override
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id = " +
                userId + " не найден"));
        Item item = ItemMapper.toItem(itemDto, user);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        User user = checkUserById(userId);
        Item item = checkItemId(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        itemDto.setComments(commentRepository.findAllByItemId(item.getId()).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));
        if (item.getOwner().getId().equals(userId)) {
            getTwoBooking(itemDto);
        }
        return itemDto;
    }

    @Transactional
    @Override
    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getItemsByOwnerId(Long userId) {
        User owner = checkUserById(userId);
        List<ItemDto> itemsForUser = itemRepository.getItemsByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        for (ItemDto itemDto : itemsForUser) {
            getTwoBooking(itemDto);
        }
        itemsForUser = itemsForUser.stream().sorted((o1, o2) -> {
            if (o1.getNextBooking() == null) {
                return 1;
            }
            if (o2.getNextBooking() == null) {
                return -1;
            }
            return o1.getNextBooking().getStart().compareTo(o2.getNextBooking().getStart());
        }).collect(Collectors.toList());
        return itemsForUser;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getItemsByTextOfQuery(String textOfQuery) {
        if (textOfQuery.isEmpty()) {
            return Collections.emptyList();
        }
        return itemRepository.getItemsByTextOfQuery(textOfQuery)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        User user = checkUserById(userId);
        Item updateItem = checkItemId(itemId);
        if (!updateItem.getOwner().getId().equals(user.getId())) {
            throw new ErrorAccess("Ошибка доступа. Только владелец может вносить изменения");
        }
        if (itemDto.getName() != null) {
            updateItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            updateItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            updateItem.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(updateItem));
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = checkUserById(userId);
        Item item = checkItemId(itemId);
        LocalDateTime time = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByBookerIdAndItemIdAndEndBeforeOrderByEndDesc(userId,
                itemId, time);
        if (bookings == null || bookings.size() == 0) {
            throw new ValidationException("Пользователь с id = " + userId +
                    " не бронировал предмет с id = " + itemId);
        }
        Comment comment = CommentMapper.toComment(commentDto, item, user, time);
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    private User checkUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь c id = " + userId + " не найден"));
    }

    private Item checkItemId(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Предмет с id = " + itemId + " не найден"));
    }

    private void getTwoBooking(ItemDto itemDto) {
        BookingDto lastBooking = null;
        Booking pastBookings = bookingRepository
                .findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(itemDto.getId(), Status.APPROVED,
                        LocalDateTime.now());
        if (pastBookings != null) {
            lastBooking = bookingMapper.toBookingByItemDto(pastBookings);
        }
        itemDto.setLastBooking(lastBooking);
        BookingDto nextBooking = null;
        Booking futureBooking = bookingRepository
                .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(itemDto.getId(), Status.APPROVED,
                        LocalDateTime.now());
        if (futureBooking != null) {
            nextBooking = bookingMapper.toBookingByItemDto(futureBooking);
        }
        itemDto.setNextBooking(nextBooking);
    }
}
