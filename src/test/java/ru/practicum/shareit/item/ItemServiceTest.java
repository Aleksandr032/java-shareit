package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ErrorAccess;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

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

    private Comment putComment(Long id) {
        return Comment.builder()
                .id(id)
                .text("Какой-то комментарий")
                .build();
    }

    private ItemRequest putItemRequest(Long id) {
        return ItemRequest.builder()
                .id(id)
                .description("Ищу молоток")
                .build();
    }

    private Booking putBooking(Long id, User booker, Item item) {
        return Booking.builder()
                .id(id)
                .status(Status.APPROVED)
                .booker(booker)
                .item(item)
                .build();
    }

    @Test
    void addItemTestCorrect() {
        User user = putUser(1L);
        ItemRequest itemRequest = putItemRequest(2L);
        Item item = putItem(3L);
        item.setOwner(user);
        item.setItemRequest(itemRequest);
        ItemDto itemDto = ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .requestId(itemRequest.getId())
                .build();
        when(userRepository.findById((user.getId()))).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        ItemDto actualDto = itemService.addItem(itemDto, user.getId());
        assertThat(actualDto.getName(), equalTo(item.getName()));
        assertThat(actualDto.getDescription(), equalTo(item.getDescription()));
        assertThat(actualDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(actualDto.getRequestId(), equalTo(itemRequest.getId()));
        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRequestRepository, times(1)).findById((itemRequest.getId()));
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void addItemTestWhenUserNotFound() {
        User user = putUser(1L);
        ItemDto itemDto = ItemDto.builder()
                .name("предмет")
                .description("описание")
                .available(true)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.addItem(itemDto, user.getId()));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void addItemTestWhenItemRequestNotFound() {
        User user = putUser(1L);
        ItemDto itemDto = ItemDto.builder()
                .name("предмет")
                .description("описание")
                .available(true)
                .requestId(user.getId())
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.addItem(itemDto, user.getId()));
        verify(itemRepository, never()).save(any(Item.class));
        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRequestRepository, times(1)).findById(anyLong());
    }

    @Test
    void getItemByIdTestWhenUserIsNotOwner() {
        User owner = putUser(1L);
        Item item = putItem(2L);
        item.setOwner(owner);
        User user = putUser(10L);
        User lastBooker = putUser(3L);
        User nextBooker = putUser(4L);
        Comment comment = putComment(5L);
        comment.setAuthor(lastBooker);
        List<Comment> comments = List.of(comment);
        Booking lastBooking = putBooking(6L, lastBooker, item);
        lastBooking.setStart(LocalDateTime.now().minusHours(10));
        lastBooking.setEnd(LocalDateTime.now().minusHours(5));
        Booking nextBooking = putBooking(7L, nextBooker, item);
        nextBooking.setStart(LocalDateTime.now().plusHours(5));
        nextBooking.setEnd(LocalDateTime.now().plusHours(10));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(item.getId())).thenReturn(comments);
        ItemDto resultDto = itemService.getItemById(user.getId(), item.getId());
        assertThat(resultDto.getId(), equalTo(item.getId()));
        assertThat(resultDto.getName(), equalTo(item.getName()));
        assertThat(resultDto.getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(resultDto.getComments().size(), equalTo(1));

        verify(itemRepository, times(1)).findById(item.getId());
        verify(commentRepository, times(1)).findAllByItemId(item.getId());
    }

    @Test
    void getItemByIdTestWhenUserIsOwner() {
        User owner = putUser(1L);
        Item item = putItem(2L);
        item.setOwner(owner);
        User lastBooker = putUser(3L);
        User nextBooker = putUser(4L);
        Comment comment = putComment(5L);
        comment.setAuthor(lastBooker);
        List<Comment> comments = List.of(comment);
        Booking lastBooking = putBooking(6L, lastBooker, item);
        lastBooking.setStatus(Status.APPROVED);
        lastBooking.setStart(LocalDateTime.now().minusHours(10));
        lastBooking.setEnd(LocalDateTime.now().minusHours(5));
        Booking nextBooking = putBooking(7L, nextBooker, item);
        nextBooking.setStart(LocalDateTime.now().plusHours(5));
        nextBooking.setEnd(LocalDateTime.now().plusHours(10));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(item.getId())).thenReturn(comments);
        when(bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(anyLong(), any(), any()))
                .thenReturn(lastBooking);
        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(anyLong(), any(), any()))
                .thenReturn(nextBooking);
        ItemDto itemDto = itemService.getItemById(owner.getId(), item.getId());
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(lastBooking.getId(), itemDto.getLastBooking().getId());
        assertEquals(nextBooking.getId(), itemDto.getNextBooking().getId());
        verify(commentRepository, times(1)).findAllByItemId(item.getId());
        verify(bookingRepository, times(1))
                .findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(anyLong(), any(), any());
        verify(bookingRepository, times(1))
                .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(anyLong(), any(), any());
    }

    @Test
    void getItemsByOwnerIdWhenUserNotFound() {
        User owner = putUser(1L);
        Integer from = 0;
        Integer size = 5;
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemsByOwnerId(owner.getId(), from, size));
        verify(itemRepository, never()).getItemsByOwnerId(owner.getId(), eq(any(Pageable.class)));
        verify(bookingRepository, never()).findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(anyLong(), any(),
                any());
        verify(bookingRepository, never()).findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(anyLong(), any(),
                any());
    }

    @Test
    void getItemsByOwnerIdWhenItemRepositoryIsEmpty() {
        User owner = putUser(1L);
        Integer from = 0;
        Integer size = 5;
        User user = putUser(2L);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(user));
        when(itemRepository.getItemsByOwnerId(anyLong(), any())).thenReturn(Collections.emptyList());
        List<ItemDto> itemsDto = itemService.getItemsByOwnerId(owner.getId(), from, size);
        assertTrue(itemsDto.isEmpty());
        verify(userRepository, times(1)).findById(owner.getId());
        verify(itemRepository, times(1)).getItemsByOwnerId(anyLong(), any());
        verify(bookingRepository, never()).findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(anyLong(), any(),
                any());
        verify(bookingRepository, never()).findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(anyLong(), any(),
                any());
    }

    @Test
    void getItemsByOwnerIdTest() {
        User owner = putUser(1L);
        User booker = putUser(2L);
        Item item = putItem(1L);
        item.setOwner(owner);
        Item item1 = putItem(12L);
        item1.setOwner(owner);
        List<Item> items = Arrays.asList(item, item1);
        Booking itemlastBooking = putBooking(10L, booker, item);
        itemlastBooking.setStart(LocalDateTime.now().minusHours(10));
        itemlastBooking.setEnd(LocalDateTime.now().minusHours(5));
        Booking itemNextBooking = putBooking(20L, booker, item);
        itemNextBooking.setStart(LocalDateTime.now().plusHours(5));
        itemNextBooking.setEnd(LocalDateTime.now().plusHours(10));
        when(userRepository.findById((owner.getId()))).thenReturn(Optional.of(owner));
        when(itemRepository.getItemsByOwnerId(eq(owner.getId()), any(Pageable.class))).thenReturn(items);
        when(bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(eq(item.getId()), any(), any()))
                .thenReturn(itemlastBooking);
        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(eq(item.getId()), any(), any()))
                .thenReturn(itemNextBooking);
        when(bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(eq(item1.getId()), any(), any()))
                .thenReturn(null);
        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(eq(item1.getId()), any(), any()))
                .thenReturn(null);
        List<ItemDto> actualItemsDto = itemService.getItemsByOwnerId(owner.getId(), 0, 10);
        assertThat(actualItemsDto.size(), equalTo(2));
        assertThat(actualItemsDto.get(0).getId(), equalTo(item.getId()));
        assertThat(actualItemsDto.get(0).getName(), equalTo(item.getName()));
        assertThat(actualItemsDto.get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(actualItemsDto.get(0).getAvailable(), equalTo(item.getAvailable()));
        assertThat(actualItemsDto.get(0).getNextBooking().getId(), equalTo(itemNextBooking.getId()));
        assertThat(actualItemsDto.get(0).getNextBooking().getBookerId(), equalTo(booker.getId()));
        assertThat(actualItemsDto.get(0).getLastBooking().getId(), equalTo(itemlastBooking.getId()));
        assertThat(actualItemsDto.get(0).getLastBooking().getBookerId(), equalTo(booker.getId()));
        assertThat(actualItemsDto.get(1).getId(), equalTo(item1.getId()));
        assertThat(actualItemsDto.get(1).getName(), equalTo(item1.getName()));
        assertThat(actualItemsDto.get(1).getDescription(), equalTo(item1.getDescription()));
        assertThat(actualItemsDto.get(1).getAvailable(), equalTo(item1.getAvailable()));
        assertThat(actualItemsDto.get(1).getNextBooking(), equalTo(null));
        assertThat(actualItemsDto.get(1).getLastBooking(), equalTo(null));
        verify(itemRepository, times(1)).getItemsByOwnerId(eq(owner.getId()), any(Pageable.class));
    }

    @Test
    void getItemsByTextOfQueryTestCorrect() {
        String textOfQuery = "Отвёртка";
        Item item = putItem(1L);
        Item item1 = putItem(2L);
        List<Item> items = Arrays.asList(item, item1);
        Integer from = 0;
        Integer size = 5;
        when(itemRepository.getItemsByTextOfQuery(eq(textOfQuery), any(Pageable.class))).thenReturn(items);
        List<ItemDto> itemsDto = itemService.getItemsByTextOfQuery(textOfQuery, from, size);
        assertThat(itemsDto.size(), equalTo(2));
        assertThat(itemsDto.get(0).getId(), equalTo(item.getId()));
        assertThat(itemsDto.get(0).getName(), equalTo(item.getName()));
        assertThat(itemsDto.get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(itemsDto.get(0).getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemsDto.get(1).getId(), equalTo(item1.getId()));
        assertThat(itemsDto.get(1).getName(), equalTo(item1.getName()));
        assertThat(itemsDto.get(1).getDescription(), equalTo(item1.getDescription()));
        assertThat(itemsDto.get(1).getAvailable(), equalTo(item1.getAvailable()));
        verify(itemRepository, times(1)).getItemsByTextOfQuery(eq(textOfQuery),
                any(Pageable.class));
    }

    @Test
    void getItemsByTextOfQueryTestWhenTextISEmpty() {
        String textOfQuery = "";
        Integer from = 0;
        Integer size = 2;
        List<ItemDto> itemsDto = itemService.getItemsByTextOfQuery(textOfQuery, from, size);
        assertTrue(itemsDto.isEmpty());
        verify(itemRepository, never()).getItemsByTextOfQuery(any(), any());
    }

    @Test
    void updateItemTestCorrect() {
        ItemDto itemDto = ItemDto.builder().build();
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        ItemDto actualDto = itemService.updateItem(item.getId(), itemDto, owner.getId());
        assertThat(actualDto.getId(), equalTo(item.getId()));
        assertThat(actualDto.getName(), equalTo(item.getName()));
        assertThat(actualDto.getDescription(), equalTo(item.getDescription()));
        assertThat(actualDto.getAvailable(), equalTo(item.getAvailable()));
        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItemTestWhenUserIsNotOwner() {
        User owner = putUser(1L);
        User user = putUser(2L);
        Item item = putItem(1L);
        item.setOwner(owner);
        ItemDto itemDto = ItemDto.builder().build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        assertThrows(ErrorAccess.class, () -> itemService.updateItem(item.getId(), itemDto, user.getId()));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItemTestWhenItemIsNotFound() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        ItemDto itemDto = ItemDto.builder().build();
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.updateItem(item.getId(), itemDto, owner.getId()));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItemTestWhenUserIsNotFound() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        ItemDto itemDto = ItemDto.builder().build();
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.updateItem(item.getId(), itemDto, owner.getId()));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void addCommentTestCorrect() {
        User owner = putUser(1L);
        User user = putUser(2L);
        Item item = putItem(1L);
        item.setOwner(owner);
        CommentDto commentDto = CommentDto.builder().text("Какой-то комментарий").build();
        Comment comment = putComment(5L);
        comment.setAuthor(user);
        Booking booking = putBooking(10L, user, item);
        booking.setStatus(Status.APPROVED);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findByBookerIdAndItemIdAndEndBeforeOrderByEndDesc(anyLong(), anyLong(), any()))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any())).thenReturn(comment);
        CommentDto actualCommentDto = itemService.addComment(user.getId(), item.getId(), commentDto);
        assertThat(actualCommentDto.getText(), equalTo(commentDto.getText()));
        verify(itemRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findByBookerIdAndItemIdAndEndBeforeOrderByEndDesc(anyLong(), anyLong(), any());
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void addCommentTestWhenUserIsNotBooker() {
        User owner = putUser(1L);
        User user = putUser(2L);
        Item item = putItem(1L);
        item.setOwner(owner);
        CommentDto commentDto = CommentDto.builder().text("Какой-то комментарий").build();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findByBookerIdAndItemIdAndEndBeforeOrderByEndDesc(anyLong(), anyLong(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> itemService.addComment(user.getId(), item.getId(), commentDto));
        verify(itemRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findByBookerIdAndItemIdAndEndBeforeOrderByEndDesc(anyLong(), anyLong(), any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteItemTestCorrect() {
        User owner = putUser(1L);
        Item item = putItem(1L);
        item.setOwner(owner);
        itemService.deleteItem(item.getId());
        verify(itemRepository, times(1)).deleteById(eq(1L));
    }
}

