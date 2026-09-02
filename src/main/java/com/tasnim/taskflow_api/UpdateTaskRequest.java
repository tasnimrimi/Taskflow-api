package com.tasnim.taskflow_api;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateTaskRequest {

    @Size(
            max = 100,
            message = "Title must be 100 characters or fewer"
    )
    @Pattern(
            regexp = "(?s).*\\S.*",
            message = "Title must not be blank"
    )
    private String title;

    private Boolean completed;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}