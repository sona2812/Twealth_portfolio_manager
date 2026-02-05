package com.demo.dto;

import com.demo.model.User;

public class UserDTO {

    private Long id;
    private String username;
    private String email;

    // Constructor
    public UserDTO(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Convert a User entity to a UserDTO
    public static UserDTO fromEntity(User user) {
        return new UserDTO((long) user.getId(), user.getUsername(), user.getEmail());
    }

    // Convert a UserDTO to a User entity (optional, if needed for updates)
    public User toEntity() {
        User user = new User();
        user.setId(Math.toIntExact(this.id));
        user.setUsername(this.username);
        user.setEmail(this.email);
        return user;
    }
}

