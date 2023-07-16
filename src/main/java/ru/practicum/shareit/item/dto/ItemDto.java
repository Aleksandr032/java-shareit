package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class ItemDto {
    private Long id;
    @NotBlank(message = "Укажите название предмета")
    private String name;
    @NotBlank(message = "Укажите описание предмета")
    private String description;
    @NotNull(message = "Укажите, доступен ли предмет")
    private Boolean available;
}
