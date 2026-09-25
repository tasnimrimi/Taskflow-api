package com.tasnim.taskflow_api;

import java.time.Instant;

import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private static final long ACCESS_TOKEN_DURATION_SECONDS = 900;

    private final JwtEncoder jwtEncoder;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }
    public long getAccessTokenDurationSeconds() {
        return ACCESS_TOKEN_DURATION_SECONDS;
    }

    public String createAccessToken(String email) {
        Instant issuedAt = Instant.now();
        Instant expiresAt =
                issuedAt.plusSeconds(ACCESS_TOKEN_DURATION_SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("taskflow-api")
                .subject(email)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        JwsHeader header = JwsHeader
                .with(SignatureAlgorithm.RS256)
                .build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        return token;
    }
}