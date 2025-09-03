package ru.practicum.shareit.item.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> itemDtoJson;

    @Autowired
    private JacksonTester<CommentDto> commentDtoJson;

    @Autowired
    private JacksonTester<CommentCreateDto> commentCreateDtoJson;

    @Test
    @DisplayName("ItemDto сериализуется со всеми полями")
    void itemDto_serialize_ok() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("ok")
                .available(true)
                .requestId(77L)
                .build();

        var json = itemDtoJson.write(dto);

        Assertions.assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        Assertions.assertThat(json).extractingJsonPathStringValue("$.description").isEqualTo("ok");
        Assertions.assertThat(json).extractingJsonPathBooleanValue("$.available").isTrue();
        Assertions.assertThat(json).extractingJsonPathNumberValue("$.requestId").isEqualTo(77);
    }

    @Test
    @DisplayName("CommentDto десериализуется из JSON")
    void commentDto_deserialize_ok() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0); // без наносекунд, чтобы сравнение прошло

        String body = "{"
                + "\"id\":2,"
                + "\"text\":\"nice\","
                + "\"authorName\":\"Ann\","
                + "\"created\":\"" + now + "\""
                + "}";

        CommentDto parsed = commentDtoJson.parse(body).getObject();

        Assertions.assertThat(parsed.getId()).isEqualTo(2L);
        Assertions.assertThat(parsed.getText()).isEqualTo("nice");
        Assertions.assertThat(parsed.getAuthorName()).isEqualTo("Ann");
        Assertions.assertThat(parsed.getCreated()).isEqualTo(now);
    }

    @Test
    @DisplayName("CommentCreateDto десериализуется из JSON")
    void commentCreateDto_deserialize_ok() throws Exception {
        String body = "{"
                + "\"text\":\"good\""
                + "}";

        CommentCreateDto parsed = commentCreateDtoJson.parse(body).getObject();
        Assertions.assertThat(parsed.getText()).isEqualTo("good");
    }
}