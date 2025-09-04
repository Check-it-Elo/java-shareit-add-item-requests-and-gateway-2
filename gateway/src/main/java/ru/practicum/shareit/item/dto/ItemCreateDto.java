package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ItemCreateDto {
    @NotBlank(message = "name must not be blank")
    private String name;

    @NotBlank(message = "description must not be blank")
    private String description;

    @NotNull(message = "available must not be null")
    private Boolean available;

    @Positive(message = "requestId must be positive")
    private Long requestId;
}