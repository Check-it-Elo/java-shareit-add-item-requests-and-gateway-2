package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private UserRepository repo;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(UserRepository.class);
        service = new UserServiceImpl(repo);
    }

    private static User user(long id, String name, String email) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        return u;
    }

    @Test
    void create_ok() {
        when(repo.existsByEmailIgnoreCase("a@ex.com")).thenReturn(false);
        when(repo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserDto out = service.create(UserDto.builder().name("Ann").email("a@ex.com").build());
        assertEquals(1L, out.getId());
        assertEquals("Ann", out.getName());
    }

    @Test
    void create_blankEmail_throwsIAE() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(UserDto.builder().name("Ann").email(" ").build()));
        assertTrue(ex.getMessage().contains("Email must not be null or blank"));
    }

    @Test
    void create_emailWithoutAt_throwsIAE() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(UserDto.builder().name("Ann").email("excom").build()));
        assertTrue(ex.getMessage().contains("Email must contain '@'"));
    }

    @Test
    void create_duplicateEmail_throwsConflict() {
        when(repo.existsByEmailIgnoreCase("a@ex.com")).thenReturn(true);
        when(repo.findAll()).thenReturn(List.of(user(2L, "Bob", "a@ex.com")));

        assertThrows(ConflictException.class,
                () -> service.create(UserDto.builder().name("Ann").email("a@ex.com").build()));
    }

    @Test
    void update_changesOnlyProvidedFields_andChecksUniqueEmail() {
        when(repo.findById(1L)).thenReturn(Optional.of(user(1L, "Old", "old@ex.com")));
        when(repo.existsByEmailIgnoreCase("new@ex.com")).thenReturn(true);
        when(repo.findAll()).thenReturn(List.of(user(2L, "Other", "new@ex.com")));

        // email занят другим — Conflict
        assertThrows(ConflictException.class,
                () -> service.update(1L, UserDto.builder().email("new@ex.com").build()));

        // а вот изменение имени без email — ок
        when(repo.findById(1L)).thenReturn(Optional.of(user(1L, "Old", "old@ex.com")));
        UserDto out = service.update(1L, UserDto.builder().name("New").build());
        assertEquals("New", out.getName());
        assertEquals("old@ex.com", out.getEmail());
    }

    @Test
    void getById_ok_and_notFound() {
        when(repo.findById(5L)).thenReturn(Optional.of(user(5L, "U5", "u5@ex.com")));
        assertEquals(5L, service.getById(5L).getId());

        when(repo.findById(6L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.getById(6L));
    }

    @Test
    void getAll_maps() {
        when(repo.findAll()).thenReturn(List.of(user(1L, "A", "a@ex.com"), user(2L, "B", "b@ex.com")));
        assertEquals(2, service.getAll().size());
    }

    @Test
    void delete_checksExists() {
        when(repo.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repo).deleteById(1L);

        when(repo.existsById(2L)).thenReturn(false);
        assertThrows(NoSuchElementException.class, () -> service.delete(2L));
    }
}