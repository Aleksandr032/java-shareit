package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto addItem(@RequestHeader(USER_ID) Long userId, @RequestBody ItemDto itemDto) {
        return itemService.addItem(itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(USER_ID) Long userId,
                               @PathVariable Long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
    }

    @GetMapping
    public List<ItemDto> getItemsByOwnerId(@RequestHeader(USER_ID) Long userId,
                                           @RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return itemService.getItemsByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByTextOfQuery(@RequestParam String text,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return itemService.getItemsByTextOfQuery(text, from, size);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId, @RequestBody ItemDto itemDto,
                              @RequestHeader(USER_ID) Long userId) {
        return itemService.updateItem(itemId, itemDto, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID) Long userId, @PathVariable Long itemId,
                                 @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}
