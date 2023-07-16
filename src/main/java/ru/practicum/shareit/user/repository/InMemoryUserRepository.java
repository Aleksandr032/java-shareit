package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.*;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long userId = 0;

    @Override
    public User addUser(User user) {
        checkFreeEmail(user.getEmail());
        user.setId(++userId);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(Long id) {
        checkUserById(id);
        return users.get(id);
    }

    @Override
    public void deleteUser(Long id) {
        checkUserById(id);
        users.remove(id);
    }

    @Override
    public User updateUser(Long userId, UserDto userDto) {
        checkUserById(userId);
        User updateUser = users.get(userId);
        updateUser.setName(userDto.getName());
        updateUser.setEmail(userDto.getEmail());
        return updateUser;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private void checkUserById(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private void checkFreeEmail(String email) {
        if (users.values().stream().anyMatch(user -> user.getEmail().equals(email))) {
            throw new ValidationException("email уже используется");
        }
    }
}
