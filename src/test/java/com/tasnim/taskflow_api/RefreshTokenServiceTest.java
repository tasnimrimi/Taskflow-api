package com.tasnim.taskflow_api;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    @Test
    void shouldCreateAndStoreHashedRefreshToken() {
        RefreshTokenRepository tokenRepository =
                mock(RefreshTokenRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        RefreshTokenService service =
                new RefreshTokenService(
                        tokenRepository,
                        userRepository
                );

        AppUser user = new AppUser(
                "learner@example.com",
                "password-hash"
        );

        when(userRepository.findByEmailIgnoreCase(
                "learner@example.com"
        )).thenReturn(Optional.of(user));

        String originalToken =
                service.createRefreshToken(
                        "learner@example.com"
                );

        ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(
                        RefreshToken.class
                );

        verify(tokenRepository)
                .save(tokenCaptor.capture());

        RefreshToken savedToken =
                tokenCaptor.getValue();

        assertFalse(originalToken.isBlank());
        assertNotEquals(
                originalToken,
                savedToken.getTokenHash()
        );
        assertEquals(
                64,
                savedToken.getTokenHash().length()
        );
        assertSame(user, savedToken.getUser());
        assertFalse(savedToken.isRevoked());
        assertTrue(
                savedToken.getExpiresAt()
                        .isAfter(Instant.now())
        );
    }

    @Test
    void shouldReturnEmailForValidRefreshToken() {
        RefreshTokenRepository tokenRepository =
                mock(RefreshTokenRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        RefreshTokenService service =
                new RefreshTokenService(
                        tokenRepository,
                        userRepository
                );

        AppUser user = new AppUser(
                "learner@example.com",
                "password-hash"
        );

        RefreshToken storedToken =
                new RefreshToken(
                        "stored-hash",
                        user,
                        Instant.now().plusSeconds(60)
                );

        when(tokenRepository.findByTokenHash(
                org.mockito.ArgumentMatchers.anyString()
        )).thenReturn(Optional.of(storedToken));

        Optional<String> result =
                service.findValidUserEmail(
                        "original-refresh-token"
                );

        assertEquals(
                Optional.of("learner@example.com"),
                result
        );
    }

    @Test
    void shouldRejectRevokedRefreshToken() {
        RefreshTokenRepository tokenRepository =
                mock(RefreshTokenRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        RefreshTokenService service =
                new RefreshTokenService(
                        tokenRepository,
                        userRepository
                );

        AppUser user = new AppUser(
                "learner@example.com",
                "password-hash"
        );

        RefreshToken storedToken =
                new RefreshToken(
                        "stored-hash",
                        user,
                        Instant.now().plusSeconds(60)
                );

        storedToken.revoke();

        when(tokenRepository.findByTokenHash(
                org.mockito.ArgumentMatchers.anyString()
        )).thenReturn(Optional.of(storedToken));

        Optional<String> result =
                service.findValidUserEmail(
                        "revoked-refresh-token"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        RefreshTokenRepository tokenRepository =
                mock(RefreshTokenRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        RefreshTokenService service =
                new RefreshTokenService(
                        tokenRepository,
                        userRepository
                );

        AppUser user = new AppUser(
                "learner@example.com",
                "password-hash"
        );

        RefreshToken storedToken =
                new RefreshToken(
                        "stored-hash",
                        user,
                        Instant.now().minusSeconds(60)
                );

        when(tokenRepository.findByTokenHash(
                org.mockito.ArgumentMatchers.anyString()
        )).thenReturn(Optional.of(storedToken));

        Optional<String> result =
                service.findValidUserEmail(
                        "expired-refresh-token"
                );

        assertTrue(result.isEmpty());
    }
    @Test
    void shouldRevokeStoredRefreshToken() {
        RefreshTokenRepository tokenRepository =
                mock(RefreshTokenRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        RefreshTokenService service =
                new RefreshTokenService(
                        tokenRepository,
                        userRepository
                );

        AppUser user = new AppUser(
                "learner@example.com",
                "password-hash"
        );

        RefreshToken storedToken =
                new RefreshToken(
                        "stored-hash",
                        user,
                        Instant.now().plusSeconds(60)
                );

        when(tokenRepository.findByTokenHash(
                org.mockito.ArgumentMatchers.anyString()
        )).thenReturn(Optional.of(storedToken));

        boolean revoked =
                service.revokeRefreshToken(
                        "original-refresh-token"
                );

        assertTrue(revoked);
        assertTrue(storedToken.isRevoked());

        verify(tokenRepository).save(storedToken);
    }
}