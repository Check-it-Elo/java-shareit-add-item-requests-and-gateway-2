package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository users;

    public UserServiceImpl(UserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        validateEmailRequired(dto.getEmail());
        validateEmailUnique(dto.getEmail(), null);
        User u = UserMapper.toUser(dto);
        u.setId(null);
        u = users.save(u);
        return UserMapper.toUserDto(u);
    }

    @Override
    @Transactional
    public UserDto update(Long userId, UserDto patch) {
        User u = users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        if (patch.getEmail() != null) {
            validateEmailRequired(patch.getEmail());
            if (!patch.getEmail().equalsIgnoreCase(u.getEmail())) {
                validateEmailUnique(patch.getEmail(), userId);
                u.setEmail(patch.getEmail());
            }
        }
        if (patch.getName() != null) {
            u.setName(patch.getName());
        }
        return UserMapper.toUserDto(u);
    }

    @Override
    public UserDto getById(Long userId) {
        return users.findById(userId)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
    }

    @Override
    public List<UserDto> getAll() {
        return users.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (!users.existsById(userId)) {
            throw new NoSuchElementException("User not found: " + userId);
        }
        users.deleteById(userId);
    }

    private void validateEmailRequired(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@'");
        }
    }

    private void validateEmailUnique(String email, Long selfId) {
        if (email == null) return;
        boolean exists = users.existsByEmailIgnoreCase(email)
                && users.findAll().stream()
                .anyMatch(u -> u.getEmail() != null
                        && u.getEmail().equalsIgnoreCase(email)
                        && (selfId == null || !u.getId().equals(selfId)));
        if (exists) {
            throw new ConflictException("Email already in use: " + email);
        }
    }

}