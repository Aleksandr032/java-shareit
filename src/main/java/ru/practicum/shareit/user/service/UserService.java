package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(UserDto userDto);

    UserDto getUserById(Long id);

    void deleteUser(Long id);

    UserDto updateUser(Long userId, UserDto userDto);

    List<UserDto> getAllUsers();
}
