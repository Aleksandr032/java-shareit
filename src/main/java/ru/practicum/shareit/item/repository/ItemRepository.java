package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface ItemRepository {
    Item addItem(Item item);

    Item getItemById(Long itemId);

    void deleteItem(Long itemId);

    List<Item> getItemsByOwnerId(Long userId);

    List<Item> getItemsByTextOfQuery(String textOfQuery);

    Item updateItem(Long itemId, ItemDto itemDto, User user);
}
