package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class UserDto {
    private Long id;
    @NotBlank(message = "Укажите своё имя пользователя")
    private String name;
    @NotBlank(message = "Укажите e-mail")
    @Email(message = "Некорректный e-mail")
    private String email;
}

