package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserRepository {
    User addUser(User user);

    User getUserById(Long id);

    void deleteUser(Long id);

    User updateUser(Long userId, UserDto userDto);

    List<User> getAllUsers();
}
