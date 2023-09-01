package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ItemRequestServiceTest {
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

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

    private ItemRequest putItemRequest(Long id) {
        return ItemRequest.builder()
                .id(id)
                .description("Ищу молоток")
                .build();
    }

    @Test
    void addItemRequestTestCorrect() {
        User user = putUser(1L);
        ItemRequest itemRequest = putItemRequest(2L);
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().build();
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        ItemRequestDto actualDto = itemRequestService.addItemRequest(user.getId(), itemRequestDto);
        assertThat(actualDto.getId(), equalTo(itemRequest.getId()));
        assertThat(actualDto.getDescription(), equalTo(itemRequest.getDescription()));
        verify(userRepository, times(1)).findById(eq(user.getId()));
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void addItemRequestTestWhenUserIsNotFound() {
        User user = putUser(1L);
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemRequestService.addItemRequest(user.getId(), itemRequestDto));
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getItemRequestByIdTestCorrect() {
        User owner = putUser(1L);
        User requester = putUser(2L);
        ItemRequest itemRequest = putItemRequest(3L);
        itemRequest.setRequester(requester);
        Item item = putItem(4L);
        item.setOwner(owner);
        item.setItemRequest(itemRequest);
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.getItemsByItemRequestId(itemRequest.getId())).thenReturn(List.of(item));
        ItemRequestDto actualDto = itemRequestService.getRequestById(requester.getId(), itemRequest.getId());
        assertThat(actualDto.getId(), equalTo(itemRequest.getId()));
        assertThat(actualDto.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(actualDto.getItems().size(), equalTo(1));
        assertThat(actualDto.getItems().get(0).getId(), equalTo(item.getId()));
        assertThat(actualDto.getItems().get(0).getName(), equalTo(item.getName()));
        assertThat(actualDto.getItems().get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(actualDto.getItems().get(0).getAvailable(), equalTo(item.getAvailable()));
        assertThat(actualDto.getItems().get(0).getRequestId(), equalTo(item.getItemRequest().getId()));
        verify(userRepository, times(1)).findById(requester.getId());
        verify(itemRequestRepository, times(1)).findById(itemRequest.getId());
        verify(itemRepository, times(1)).getItemsByItemRequestId(anyLong());
    }

    @Test
    void getItemRequestByIdTestWhenUserNotFound() {
        User user = putUser(1L);
        ItemRequest itemRequest = putItemRequest(5L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(user.getId(),
                itemRequest.getId()));
        verify(itemRequestRepository, never()).findById(any());
        verify(itemRepository, never()).getItemsByItemRequestId(anyLong());
    }

    @Test
    void getItemRequestByIdTestWhenItemRequestNotFound() {
        User user = putUser(1L);
        ItemRequest itemRequest = putItemRequest(5L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(user.getId(),
                itemRequest.getId()));
        verify(itemRepository, never()).getItemsByItemRequestId(anyLong());
    }

    @Test
    void getAllItemRequestsTestCorrect() {
        User owner = putUser(1L);
        User requester = putUser(2L);
        ItemRequest itemRequest = putItemRequest(3L);
        itemRequest.setRequester(requester);
        ItemRequest itemRequest1 = putItemRequest(4L);
        itemRequest1.setRequester(requester);
        Item item = putItem(5L);
        item.setOwner(owner);
        item.setItemRequest(itemRequest);
        Item item1 = putItem(6L);
        item1.setOwner(owner);
        item1.setItemRequest(itemRequest1);
        List<ItemRequest> itemRequests = List.of(itemRequest, itemRequest1);
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(anyLong(), any())).thenReturn(itemRequests);
        List<ItemRequestDto> actualDto = itemRequestService.getAllItemRequests(requester.getId(), 0, 10);
        when(itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(anyLong(), any())).thenReturn(itemRequests);
        when(itemRepository.getItemsByItemRequestId(eq(itemRequest.getId()))).thenReturn(List.of(item));
        when(itemRepository.getItemsByItemRequestId(eq(itemRequest1.getId()))).thenReturn(List.of(item1));
        assertThat(actualDto.size(), equalTo(2));
        assertThat(actualDto.get(0).getId(), equalTo(itemRequest.getId()));
        assertThat(actualDto.get(1).getId(), equalTo(itemRequest1.getId()));
        verify(itemRequestRepository, times(1))
                .findByRequesterIdNotOrderByCreatedDesc(anyLong(), any());
        verify(itemRepository, times(2)).getItemsByItemRequestId(anyLong());
    }

    @Test
    void getAllItemRequestsTestWhenItemRequestsIsEmpty() {
        User user = putUser(1L);
        when(itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(anyLong(),
                any())).thenReturn(Collections.emptyList());
        List<ItemRequestDto> itemRequestsDto = itemRequestService.getAllItemRequests(user.getId(), 0, 10);
        assertTrue(itemRequestsDto.isEmpty());
        verify(itemRequestRepository, times(1))
                .findByRequesterIdNotOrderByCreatedDesc(anyLong(), any());
        verify(itemRepository, never()).getItemsByItemRequestId(anyLong());
    }

    @Test
    void getAllRequestsByUserIdTestCorrect() {
        User requester = putUser(2L);
        ItemRequest itemRequest = putItemRequest(3L);
        itemRequest.setRequester(requester);
        ItemRequest itemRequest1 = putItemRequest(4L);
        itemRequest1.setRequester(requester);
        List<ItemRequest> itemRequests = List.of(itemRequest, itemRequest1);
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(eq(requester.getId()))).thenReturn(itemRequests);
        List<ItemRequestDto> actualDto = itemRequestService.getAllRequestsByUserId(requester.getId());
        assertThat(actualDto.size(), equalTo(2));
        assertThat(actualDto.get(0).getId(), equalTo(itemRequest.getId()));
        assertThat(actualDto.get(0).getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(actualDto.get(1).getId(), equalTo(itemRequest1.getId()));
        assertThat(actualDto.get(1).getDescription(), equalTo(itemRequest1.getDescription()));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findByRequesterIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getAllRequestsByUserIdTestWhenUserNotFound() {
        User requester = putUser(2L);
        ItemRequest itemRequest = putItemRequest(3L);
        itemRequest.setRequester(requester);
        ItemRequest itemRequest1 = putItemRequest(4L);
        itemRequest1.setRequester(requester);
        when(userRepository.findById(requester.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemRequestService.getAllRequestsByUserId(requester.getId()));
        verify(itemRequestRepository, never()).findByRequesterIdOrderByCreatedDesc(any());
    }
}
