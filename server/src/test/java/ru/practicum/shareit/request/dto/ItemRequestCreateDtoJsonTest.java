package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestCreateDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestCreateDto> json;

    @Test
    @DisplayName("ItemRequestCreateDto десериализуется из JSON по полю description")
    void deserialize_ItemRequestCreateDto_ok() throws Exception {
        String body = """
                {
                  "description": "Нужна дрель"
                }
                """;

        var parsed = json.parse(body).getObject();

        assertThat(parsed.getDescription()).isEqualTo("Нужна дрель");
    }
}