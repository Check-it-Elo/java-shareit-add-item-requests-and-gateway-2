package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    private Long id;

    @NotBlank(message = "name must not be blank")
    private String name;

    @Email(message = "email must be valid")
    @NotBlank(message = "email must not be blank")
    private String email;
}