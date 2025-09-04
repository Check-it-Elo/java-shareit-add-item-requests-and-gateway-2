package ru.practicum.shareit.user;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Validated
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @PostMapping
    public UserDto create(@RequestBody UserDto dto) {
        return users.create(dto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto dto) {
        return users.update(id, dto);
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return users.getById(id);
    }

    @GetMapping
    public List<UserDto> all() {
        return users.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        users.delete(id);
    }
}