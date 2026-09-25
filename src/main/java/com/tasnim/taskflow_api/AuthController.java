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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "Register and authenticate application users"
)
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
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
    @Operation(summary = "Log in and receive access and refresh tokens")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        String email = authentication.getName();

        String accessToken =
                tokenService.createAccessToken(email);

        String refreshToken =
                refreshTokenService.createRefreshToken(email);

        TokenResponse response = new TokenResponse(
                accessToken,
                tokenService.getAccessTokenDurationSeconds(),
                refreshToken,
                refreshTokenService
                        .getRefreshTokenDurationSeconds()
        );

        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Get a new access token")
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        String email = refreshTokenService
                .findValidUserEmail(
                        request.getRefreshToken()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid or expired refresh token"
                        )
                );

        String accessToken =
                tokenService.createAccessToken(email);

        AccessTokenResponse response =
                new AccessTokenResponse(
                        accessToken,
                        tokenService
                                .getAccessTokenDurationSeconds()
                );

        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Log out and revoke a refresh token")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        refreshTokenService.revokeRefreshToken(
                request.getRefreshToken()
        );

        return ResponseEntity.noContent().build();
    }
}
