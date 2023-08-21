package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long userId);

    ItemDto getItemById(Long userId, Long itemId);

    void deleteItem(Long itemId);

    List<ItemDto> getItemsByOwnerId(Long userId, Integer from, Integer size);

    List<ItemDto> getItemsByTextOfQuery(String textOfQuery, Integer from, Integer size);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
