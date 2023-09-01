package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingDtoTest {
    @Autowired
    private JacksonTester<BookingDto> jacksonTester;
    private final LocalDateTime time = LocalDateTime
            .of(2023, Month.AUGUST, 5, 5, 5, 5);

    @Test
    void testBookingDto() throws IOException {
        LocalDateTime start = time;
        LocalDateTime end = time.plusHours(10);
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Подкова")
                .description("Бережёт копыта и приносит удачу")
                .available(true)
                .requestId(2L)
                .build();
        UserDto userDto = UserDto.builder()
                .id(2L)
                .name("Bill")
                .email("user@.test")
                .build();
        BookingDto bookingDto = BookingDto.builder()
                .id(3L)
                .start(start)
                .end(end)
                .item(itemDto)
                .booker(userDto)
                .status(Status.WAITING)
                .build();

        JsonContent<BookingDto> jsonContent = jacksonTester.write(bookingDto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(3);
        assertThat(jsonContent).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        assertThat(jsonContent).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        assertThat(jsonContent).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(jsonContent).extractingJsonPathStringValue("$.status")
                .isEqualTo(Status.WAITING.toString());
    }
}
