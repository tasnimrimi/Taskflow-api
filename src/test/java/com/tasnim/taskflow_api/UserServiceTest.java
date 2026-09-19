package com.tasnim.taskflow_api;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;

class UserServiceTest {

    @Test
    void shouldNormalizeEmailHashPasswordAndSaveUser() {
        AppUserRepository repository =
                mock(AppUserRepository.class);

        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);

        UserService service =
                new UserService(repository, passwordEncoder);

        when(repository.existsByEmailIgnoreCase(
                "learner@example.com"
        )).thenReturn(false);

        when(passwordEncoder.encode("Learning123!"))
                .thenReturn("secure-hash");

        when(repository.save(any(AppUser.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        AppUser result = service.registerUser(
                "  Learner@Example.com  ",
                "Learning123!"
        );

        assertEquals(
                "learner@example.com",
                result.getEmail()
        );

        assertEquals(
                "secure-hash",
                result.getPasswordHash()
        );

        verify(passwordEncoder)
                .encode("Learning123!");

        verify(repository)
                .save(any(AppUser.class));
    }
    @Test
    void shouldRejectAlreadyRegisteredEmail() {
        AppUserRepository repository =
                mock(AppUserRepository.class);

        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);

        UserService service =
                new UserService(repository, passwordEncoder);

        when(repository.existsByEmailIgnoreCase(
                "learner@example.com"
        )).thenReturn(true);

        AppUser result = service.registerUser(
                "Learner@Example.com",
                "Learning123!"
        );

        assertNull(result);

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(repository, never())
                .save(any(AppUser.class));
    }
}