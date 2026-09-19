package com.tasnim.taskflow_api;

public class UserResponse {

    private final Long id;
    private final String email;

    public UserResponse(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}