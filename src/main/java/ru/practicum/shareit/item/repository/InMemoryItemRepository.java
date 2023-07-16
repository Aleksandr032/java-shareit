package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ErrorAccess;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final HashMap<Long, Item> items = new HashMap<>();
    private long id = 0;

    @Override
    public Item addItem(Item item) {
        item.setId(++id);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getItemById(Long itemId) {
        checkItemId(itemId);
        return items.get(itemId);
    }

    @Override
    public void deleteItem(Long itemId) {
        checkItemId(itemId);
        items.remove(itemId);
    }

    @Override
    public List<Item> getItemsByOwnerId(Long userId) {
        return items.values()
                .stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getItemsByTextOfQuery(String textOfQuery) {
        String text = textOfQuery.toLowerCase();
        return items.values()
                .stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text)
                        || item.getDescription().toLowerCase().contains(text))
                .collect(Collectors.toList());
    }

    @Override
    public Item updateItem(Long itemId, ItemDto itemDto, User user) {
        checkItemId(itemId);
        Item updateItem = items.get(itemId);
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
        return updateItem;
    }

    private void checkItemId(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException("Предмет с id = " + itemId + " не найден");
        }
    }
}
