package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.EmailBusyException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    private UserDto dto;
    private User user;
    private Long userId;

    @BeforeEach
    void putUser() {
        userId = 1L;

        dto = UserDto.builder()
                .id(userId)
                .name("Bill")
                .email("test@email.ru")
                .build();

        user = User.builder()
                .id(userId)
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    @Test
    void addUserCorrectTest() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserDto saveDto = userService.addUser(dto);
        assertThat(saveDto.getId(), equalTo(user.getId()));
        assertThat(saveDto.getName(), equalTo(user.getName()));
        assertThat(saveDto.getEmail(), equalTo(user.getEmail()));
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void addUserTestWhenNameIsIncorrect() {
        UserDto userIncorrectName = UserDto.builder()
                .name("")
                .email("incorrect@test.test")
                .build();
        assertThrows(ValidationException.class, () -> userService.addUser(userIncorrectName));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addUserTestWhenEmailIsBusy() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User userIncorrectEmail = User.builder()
                .id(2L)
                .name("Ben")
                .email("test@email.ru")
                .build();
        UserDto userDtoIncorrectEmail = UserMapper.toUserDto(userIncorrectEmail);
        when(userRepository.save(userIncorrectEmail)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(EmailBusyException.class, () -> userService.addUser(userDtoIncorrectEmail));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void addUserTestWhenEmailIsBlank() {
        UserDto userIncorrectEmail = UserDto.builder()
                .name("Ben")
                .email(" ")
                .build();
        assertThrows(ValidationException.class, () -> userService.addUser(userIncorrectEmail));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByIdCorrectTest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserDto userDto = userService.getUserById(1L);
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getEmail(), userDto.getEmail());
        assertEquals(user.getName(), userDto.getName());
    }

    @Test
    void getUserByIdIncorrectTest() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserById(2L));
    }

    @Test
    void getAllUsersCorrectTest() {
        User user2 = User.builder()
                .id(2L)
                .name("Ben")
                .email("test2@email.ru")
                .build();
        List<User> users = List.of(user, user2);
        when(userRepository.findAll()).thenReturn(users);
        List<UserDto> usersDto = userService.getAllUsers();
        assertEquals(2, usersDto.size());
    }

    @Test
    void getAllUsersEmptyTest() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<UserDto> users = userService.getAllUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    void updateUserCorrectTestNewNameAndNewEmail() {
        User updateUser = User.builder()
                .id(1L)
                .name("Ben")
                .email("test2@email.ru")
                .build();
        UserDto updateUserToDto = UserMapper.toUserDto(updateUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updateUser);
        UserDto updatedUserDto = userService.updateUser(userId, updateUserToDto);
        assertEquals(userId, updatedUserDto.getId());
        assertEquals(updateUser.getName(), updatedUserDto.getName());
        assertEquals(updateUser.getEmail(), updatedUserDto.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateTestWhenEmailIsBusy() {
        User updateUser = User.builder()
                .id(1L)
                .name("Ben")
                .email("test@email.ru")
                .build();
        UserDto updateUserToDto = UserMapper.toUserDto(updateUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(updateUser));
        when(userRepository.save(updateUser)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(EmailBusyException.class, () -> userService.updateUser(userId, updateUserToDto));
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void deleteUserCorrectTest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        userService.deleteUser(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUserIncorrectIdTest() {
        when(userRepository.findById(5L)).thenThrow(new NotFoundException("Пользователь c id = " + userId
                + " не найден"));
        assertThrows(NotFoundException.class, () -> userService.deleteUser(5L));
    }
}