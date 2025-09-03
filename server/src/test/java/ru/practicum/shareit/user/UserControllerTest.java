package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean UserService users;

    @Test
    @DisplayName("POST /users — create возвращает созданного пользователя")
    void create_ok() throws Exception {
        var req = UserDto.builder().name("Ann").email("a@ex.com").build();
        var resp = UserDto.builder().id(1L).name("Ann").email("a@ex.com").build();

        when(users.create(any(UserDto.class))).thenReturn(resp);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(users).create(any(UserDto.class));
    }

    @Test
    @DisplayName("PATCH /users/{id} — update частично обновляет")
    void update_ok() throws Exception {
        var patch = UserDto.builder().name("New").build();
        var resp = UserDto.builder().id(5L).name("New").email("u@ex.com").build();

        when(users.update(eq(5L), any(UserDto.class))).thenReturn(resp);

        mvc.perform(patch("/users/{id}", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));

        verify(users).update(eq(5L), any(UserDto.class));
    }

    @Test
    @DisplayName("GET /users/{id} — getById")
    void getById_ok() throws Exception {
        var resp = UserDto.builder().id(3L).name("U3").email("u3@ex.com").build();
        when(users.getById(3L)).thenReturn(resp);

        mvc.perform(get("/users/{id}", 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("u3@ex.com"));

        verify(users).getById(3L);
    }

    @Test
    @DisplayName("GET /users — getAll")
    void getAll_ok() throws Exception {
        when(users.getAll()).thenReturn(List.of(
                UserDto.builder().id(1L).name("A").email("a@ex.com").build(),
                UserDto.builder().id(2L).name("B").email("b@ex.com").build()
        ));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(users).getAll();
    }

    @Test
    @DisplayName("DELETE /users/{id} — delete")
    void delete_ok() throws Exception {
        mvc.perform(delete("/users/{id}", 9))
                .andExpect(status().isOk());

        verify(users).delete(9L);
    }
}