package com.tasnim.taskflow_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.Mockito.verifyNoInteractions;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void shouldRegisterUserAndReturnSafeResponse() throws Exception {
        AppUser savedUser = mock(AppUser.class);

        when(savedUser.getId()).thenReturn(1L);
        when(savedUser.getEmail())
                .thenReturn("learner@example.com");

        when(userService.registerUser(
                "learner@example.com",
                "Learning123!"
        )).thenReturn(savedUser);

        String requestBody = """
                {
                  "email": "learner@example.com",
                  "password": "Learning123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("learner@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(userService.registerUser(
                "learner@example.com",
                "Learning123!"
        )).thenReturn(null);

        String requestBody = """
            {
              "email": "learner@example.com",
              "password": "Learning123!"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }
    @Test
    void shouldReturn400ForInvalidEmail() throws Exception {
        String requestBody = """
            {
              "email": "not-an-email",
              "password": "Learning123!"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.field")
                        .value("email"))
                .andExpect(jsonPath("$.message")
                        .value("Email must be valid"));
    }
    @Test
    void shouldReturn400ForShortPassword() throws Exception {
        String requestBody = """
            {
              "email": "learner@example.com",
              "password": "short"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.field")
                        .value("password"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Password must be between 8 and 72 characters"
                        ));
    }
    @Test
    void shouldLoginAndReturnAccessToken() throws Exception {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("learner@example.com");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(tokenService.createAccessToken(
                "learner@example.com"
        )).thenReturn(
                new TokenResponse(
                        "signed.jwt.token",
                        900
                )
        );

        String requestBody = """
            {
              "email": "learner@example.com",
              "password": "Learning123!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("signed.jwt.token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(900));
    }
    @Test
    void shouldReturn401ForInvalidLogin() throws Exception {
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(
                new BadCredentialsException("Bad credentials")
        );

        String requestBody = """
            {
              "email": "learner@example.com",
              "password": "WrongPassword!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));

        verifyNoInteractions(tokenService);
    }
}