package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    private Long id;
    @NotNull(message = "Укажите дату начала бронирования")
    @Future(message = "Дата начала бронирования должна быть предстоящей датой")
    private LocalDateTime start;
    @NotNull(message = "Укажите дату окончания бронирования")
    @Future(message = "Дата окончания бронирования должна быть предстоящей датой")
    private LocalDateTime end;

    @AssertTrue(message = "Дата окончания бронирования не может раньше даты начала бронирования")
    private boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }

    private ItemDto item;
    private Long itemId;
    private Long bookerId;
    private UserDto booker;
    private Status status;
}
