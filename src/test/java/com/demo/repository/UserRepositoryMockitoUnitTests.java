package com.demo.repository;

import com.demo.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryMockitoUnitTests {

    private final UserRepository userRepository = mock(UserRepository.class);

    @Test
    @DisplayName("findByUsername returns user when username exists")
    void findByUsernameReturnsUserWhenUsernameExists() {
        User u = new User();
        u.setId(1);
        u.setUsername("alice");
        u.setPassword("pw");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        Optional<User> fetched = userRepository.findByUsername("alice");

        assertThat(fetched).isPresent();
        assertThat(fetched.get().getUsername()).isEqualTo("alice");
        verify(userRepository, times(1)).findByUsername("alice");
    }

    @Test
    @DisplayName("findByUsername returns empty when username not found")
    void findByUsernameReturnsEmptyWhenUsernameNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<User> fetched = userRepository.findByUsername("unknown");

        assertThat(fetched).isNotPresent();
        verify(userRepository, times(1)).findByUsername("unknown");
    }

    @Test
    @DisplayName("save returns persisted user with generated id")
    void saveReturnsPersistedUserWithGeneratedId() {
        User in = new User();
        in.setUsername("bob");

        User saved = new User();
        saved.setId(7);
        saved.setUsername("bob");

        when(userRepository.save(in)).thenReturn(saved);

        User result = userRepository.save(in);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(7);
        assertThat(result.getUsername()).isEqualTo("bob");
        verify(userRepository, times(1)).save(in);
    }

    @Test
    @DisplayName("findAll returns all saved users")
    void findAllReturnsAllSavedUsers() {
        User a = new User();
        a.setId(2);
        a.setUsername("u1");
        User b = new User();
        b.setId(3);
        b.setUsername("u2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(a, b));

        List<User> all = userRepository.findAll();

        assertThat(all).hasSize(2)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("u1", "u2");

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("deleteById invokes repository delete without error")
    void deleteByIdInvokesRepositoryDeleteWithoutError() {
        doNothing().when(userRepository).deleteById(5L);

        userRepository.deleteById(5L);

        verify(userRepository, times(1)).deleteById(5L);
    }
}
