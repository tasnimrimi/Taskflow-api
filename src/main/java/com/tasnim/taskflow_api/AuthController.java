package com.tasnim.taskflow_api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "Register and authenticate application users"
)
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterUserRequest request
    ) {
        AppUser savedUser = userService.registerUser(
                request.getEmail(),
                request.getPassword()
        );

        if (savedUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        UserResponse response = new UserResponse(
                savedUser.getId(),
                savedUser.getEmail()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}