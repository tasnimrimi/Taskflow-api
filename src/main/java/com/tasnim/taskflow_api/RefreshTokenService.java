package com.tasnim.taskflow_api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private static final long
            REFRESH_TOKEN_DURATION_SECONDS = 604800;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;
    private final SecureRandom secureRandom;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            AppUserRepository appUserRepository
    ) {
        this.refreshTokenRepository =
                refreshTokenRepository;

        this.appUserRepository =
                appUserRepository;

        this.secureRandom = new SecureRandom();
    }

    @Transactional
    public String createRefreshToken(String email) {
        AppUser user = appUserRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String originalToken = Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = hashToken(originalToken);

        RefreshToken refreshToken =
                new RefreshToken(
                        tokenHash,
                        user,
                        Instant.now().plusSeconds(
                                REFRESH_TOKEN_DURATION_SECONDS
                        )
                );

        refreshTokenRepository.save(refreshToken);

        return originalToken;
    }

    @Transactional(readOnly = true)
    public Optional<String> findValidUserEmail(
            String originalToken
    ) {
        String tokenHash = hashToken(originalToken);

        return refreshTokenRepository
                .findByTokenHash(tokenHash)
                .filter(refreshToken ->
                        !refreshToken.isRevoked()
                )
                .filter(refreshToken ->
                        refreshToken
                                .getExpiresAt()
                                .isAfter(Instant.now())
                )
                .map(refreshToken ->
                        refreshToken
                                .getUser()
                                .getEmail()
                );
    }
    @Transactional
    public boolean revokeRefreshToken(
            String originalToken
    ) {
        String tokenHash = hashToken(originalToken);

        Optional<RefreshToken> storedToken =
                refreshTokenRepository
                        .findByTokenHash(tokenHash);

        if (storedToken.isEmpty()) {
            return false;
        }

        RefreshToken refreshToken =
                storedToken.get();

        if (refreshToken.isRevoked()) {
            return false;
        }

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        return true;
    }

    public long getRefreshTokenDurationSeconds() {
        return REFRESH_TOKEN_DURATION_SECONDS;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashedBytes = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat
                    .of()
                    .formatHex(hashedBytes);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }
}