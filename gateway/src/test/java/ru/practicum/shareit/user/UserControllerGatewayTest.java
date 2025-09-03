//package ru.practicum.shareit.user;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import ru.practicum.shareit.user.dto.UserDto;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(controllers = UserController.class)
//class UserControllerGatewayTest {
//
//    @Autowired
//    private MockMvc mvc;
//
//    @Autowired
//    private ObjectMapper mapper;
//
//    @MockBean
//    private UserClient userClient;
//
//    @Test
//    @DisplayName("POST /users с неверным email — 400 (валидация)")
//    void create_badEmail_returns400() throws Exception {
//        String body = mapper.writeValueAsString(UserDto.builder()
//                .name("Ann")
//                .email("wrong_email")
//                .build());
//
//        mvc.perform(post("/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("POST /users валидный — уходит в клиент, 201/200")
//    void create_ok_callsClient() throws Exception {
//        String body = mapper.writeValueAsString(UserDto.builder()
//                .name("Ann")
//                .email("a@ex.com")
//                .build());
//
//        mvc.perform(post("/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().is2xxSuccessful());
//
//        verify(userClient).create(any(UserDto.class));
//    }
//}