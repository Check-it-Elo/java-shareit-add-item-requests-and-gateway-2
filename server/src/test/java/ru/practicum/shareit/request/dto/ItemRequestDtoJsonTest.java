package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;

import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    @DisplayName("ItemRequestDto сериализуется: id, description, created, items[]")
    void serialize_ItemRequestDto_ok() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(100L);
        item.setName("Перфоратор");

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(11L);
        dto.setDescription("Ищу перфоратор");
        dto.setCreated(LocalDateTime.of(2025, 1, 2, 3, 4, 5));
        dto.setItems(List.of(item));

        var content = json.write(dto);

        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(11);
        assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo("Ищу перфоратор");
        // ISO-8601 без таймзоны (по умолчанию у Jackson в Boot)
        assertThat(content).extractingJsonPathStringValue("$.created").startsWith("2025-01-02T03:04:05");
        assertThat(content).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(content).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(100);
        assertThat(content).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Перфоратор");
    }
}