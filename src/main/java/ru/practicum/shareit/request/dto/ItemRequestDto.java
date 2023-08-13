package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDto {
    private Long id;
    @NotBlank(message = "Укажите описание")
    @Size(max = 512, message = "Описание слишком длинное. Попробуйте сформулирвоать его в пределах 512 знаков")
    private String description;
    private LocalDateTime created;
    private List<ItemDto> items;
}
