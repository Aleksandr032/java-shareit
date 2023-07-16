package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long userId);

    ItemDto getItemById(Long itemId);

    void deleteItem(Long itemId);

    List<ItemDto> getItemsByOwnerId(Long userId);

    List<ItemDto> getItemsByTextOfQuery(String textOfQuery);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId);
}
