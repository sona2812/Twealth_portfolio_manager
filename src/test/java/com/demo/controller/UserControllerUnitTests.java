package com.demo.controller;

import com.demo.model.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTests {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("createUser returns the saved user from the service")
    void createUserReturnsSavedUser() {
        User input = new User(0, "alice", "pass", "alice@example.com");
        User saved = new User(1, "alice", "pass", "alice@example.com");

        when(userService.saveUser(input)).thenReturn(saved);

        User result = userController.createUser(input);

        assertThat(result).isEqualTo(saved);
        verify(userService, times(1)).saveUser(input);
    }

    @Test
    @DisplayName("createUser handles null input by delegating to service")
    void createUserHandlesNullInputByDelegatingToService() {
        when(userService.saveUser(null)).thenReturn(null);

        User result = userController.createUser(null);

        assertThat(result).isNull();
        verify(userService, times(1)).saveUser(null);
    }

    @Test
    @DisplayName("getAllUsers returns all users from the service")
    void getAllUsersReturnsAllUsers() {
        User a = new User(1, "u1", "p1", "u1@example.com");
        User b = new User(2, "u2", "p2", "u2@example.com");

        when(userService.getAllUsers()).thenReturn(Arrays.asList(a, b));

        List<User> result = userController.getAllUsers();

        assertThat(result).containsExactlyInAnyOrder(a, b);
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("getAllUsers returns empty list when service has no users")
    void getAllUsersReturnsEmptyListWhenNoUsers() {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        List<User> result = userController.getAllUsers();

        assertThat(result).isEmpty();
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("getUserById returns optional user when present")
    void getUserByIdReturnsOptionalWhenPresent() {
        User u = new User(5, "bob", "pw", "bob@example.com");
        when(userService.getUserById(5L)).thenReturn(Optional.of(u));

        Optional<User> result = userController.getUserById(5L);

        assertThat(result).isPresent().contains(u);
        verify(userService, times(1)).getUserById(5L);
    }

    @Test
    @DisplayName("getUserById returns empty optional when missing")
    void getUserByIdReturnsEmptyWhenMissing() {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userController.getUserById(99L);

        assertThat(result).isNotPresent();
        verify(userService, times(1)).getUserById(99L);
    }

    @Test
    @DisplayName("getUserByUsername returns optional user when present")
    void getUserByUsernameReturnsOptionalWhenPresent() {
        User u = new User(7, "carol", "pw", "carol@example.com");
        when(userService.getUserByUsername("carol")).thenReturn(Optional.of(u));

        Optional<User> result = userController.getUserByUsername("carol");

        assertThat(result).isPresent().contains(u);
        verify(userService, times(1)).getUserByUsername("carol");
    }

    @Test
    @DisplayName("getUserByUsername returns empty when username not found")
    void getUserByUsernameReturnsEmptyWhenNotFound() {
        when(userService.getUserByUsername("nope")).thenReturn(Optional.empty());

        Optional<User> result = userController.getUserByUsername("nope");

        assertThat(result).isNotPresent();
        verify(userService, times(1)).getUserByUsername("nope");
    }

    @Test
    @DisplayName("deleteUserById delegates to service")
    void deleteUserByIdDelegatesToService() {
        doNothing().when(userService).deleteUserById(11L);

        userController.deleteUserById(11L);

        verify(userService, times(1)).deleteUserById(11L);
    }

    @Test
    @DisplayName("deleteUserByUsername delegates to service")
    void deleteUserByUsernameDelegatesToService() {
        doNothing().when(userService).deleteUserByUsername("tina");

        userController.deleteUserByUsername("tina");

        verify(userService, times(1)).deleteUserByUsername("tina");
    }
}
