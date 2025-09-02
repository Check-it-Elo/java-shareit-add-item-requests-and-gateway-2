package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Service
public class UserClient extends BaseClient {

    public UserClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/users"))
                .build());
    }

    public ResponseEntity<Object> create(UserDto dto) {
        return post("", 0L, dto);
    }

    public ResponseEntity<Object> update(long userId, UserDto dto) {
        return patch("/" + userId, 0L, dto);
    }

    public ResponseEntity<Object> getById(long userId) {
        return get("/" + userId, 0L);
    }

    public ResponseEntity<Object> getAll() {
        return get("", 0L);
    }

    public ResponseEntity<Object> delete(long userId) {
        return delete("/" + userId, 0L);
    }

}