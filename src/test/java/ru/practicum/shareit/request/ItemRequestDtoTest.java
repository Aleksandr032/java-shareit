package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ItemRequestDtoTest {
    @Autowired
    private JacksonTester<ItemRequestDto> jacksonTester;

    @Test
    void testItemRequestDto() throws IOException {
        LocalDateTime created = LocalDateTime.now().withNano(0);
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Описание")
                .created(created)
                .items(List.of(
                        ItemDto.builder()
                                .id(2L)
                                .name("Молоток")
                                .description("Советский")
                                .available(true)
                                .requestId(1L)
                                .build()
                ))
                .build();

        JsonContent<ItemRequestDto> jsonContent = jacksonTester.write(itemRequestDto);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Описание");
        assertThat(jsonContent).extractingJsonPathStringValue("$.created")
                .isEqualTo(created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        assertThat(jsonContent).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.items.[0].id").isEqualTo(2);
    }
}
