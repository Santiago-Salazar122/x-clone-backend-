package com.x_clone.backend.dto;

public class UserFollowDTO {

    private Long id;
    private String username;
    private String name;

    public UserFollowDTO(Long id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }
}