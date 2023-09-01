package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping("/items")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(USER_ID) Long userId,
                                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Создание предмета пользователем userId={}", userId);
        return itemClient.addItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_ID) Long userId,
                                              @PathVariable Long itemId) {
        log.info("Создание предмета itemId {}, userId={}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        log.info("Удаление предмета itemId={}", itemId);
        itemClient.deleteItem(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwnerId(@RequestHeader(USER_ID) Long userId,
                                                    @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                    @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получение предметов пользователя userId={}", userId);
        return itemClient.getItemsByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsByTextOfQuery(@RequestHeader(USER_ID) Long userId,
                                                        @RequestParam String text,
                                                        @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                        @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получение предмета text={}", text);
        return itemClient.getItemsByTextOfQuery(userId, text, from, size);
    }


    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable Long itemId,
                                             @RequestHeader(USER_ID) Long userId,
                                             @RequestBody ItemDto itemDto) {
        log.info("Обновление предмета itemId={}, пользователя userId={}", itemId, userId);
        return itemClient.updateItem(itemId, userId, itemDto);
    }


    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID) Long userId,
                                             @PathVariable Long itemId, @Valid @RequestBody CommentDto commentDto) {
        log.info("Добавление комментария пользователем userId={}", userId);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}