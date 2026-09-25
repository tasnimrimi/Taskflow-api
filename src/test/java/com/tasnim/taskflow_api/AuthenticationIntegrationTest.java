package com.tasnim.taskflow_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:taskflow-authentication"
})
@AutoConfigureMockMvc
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldAllowRegisteredUserWithCorrectPassword()
            throws Exception {

        AppUser user = new AppUser(
                "learner@example.com",
                passwordEncoder.encode("Learning123!")
        );

        appUserRepository.saveAndFlush(user);

        String requestBody = """
            {
              "email": "learner@example.com",
              "password": "Learning123!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectRegisteredUserWithWrongPassword()
            throws Exception {

        AppUser user = new AppUser(
                "learner@example.com",
                passwordEncoder.encode("Learning123!")
        );

        appUserRepository.saveAndFlush(user);

        String requestBody = """
            {
              "email": "learner@example.com",
              "password": "WrongPassword!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void shouldRejectUnknownEmail() throws Exception {
        String requestBody = """
            {
              "email": "unknown@example.com",
              "password": "Learning123!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void shouldLoginAndUseJwtToAccessProtectedTasks()
            throws Exception {

        String requestBody = """
            {
              "email": "jwt-user@example.com",
              "password": "Learning123!"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        MvcResult loginResult =
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andExpect(status().isOk())
                        .andReturn();

        String accessToken = JsonPath.read(
                loginResult.getResponse()
                        .getContentAsString(),
                "$.accessToken"
        );

        mockMvc.perform(get("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isOk());
    }
    @Test
    void shouldRejectInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer this-is-not-a-valid-jwt"
                        ))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void shouldRejectExpiredBearerToken() throws Exception {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("taskflow-api")
                .subject("expired-user@example.com")
                .issuedAt(now.minusSeconds(120))
                .expiresAt(now.minusSeconds(60))
                .build();

        JwsHeader header = JwsHeader
                .with(SignatureAlgorithm.RS256)
                .build();

        String expiredToken = jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                )
                .getTokenValue();

        mockMvc.perform(get("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + expiredToken
                        ))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void shouldRefreshAccessTokenThroughCompleteApplication()
            throws Exception {

        String credentials = """
            {
              "email": "refresh-user@example.com",
              "password": "Learning123!"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isCreated());

        MvcResult loginResult =
                mockMvc.perform(post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(credentials))
                        .andExpect(status().isOk())
                        .andReturn();

        String refreshToken = JsonPath.read(
                loginResult.getResponse()
                        .getContentAsString(),
                "$.refreshToken"
        );

        String refreshRequest = """
            {
              "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        MvcResult refreshResult =
                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(refreshRequest))
                        .andExpect(status().isOk())
                        .andReturn();

        String newAccessToken = JsonPath.read(
                refreshResult.getResponse()
                        .getContentAsString(),
                "$.accessToken"
        );

        mockMvc.perform(get("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + newAccessToken
                        ))
                .andExpect(status().isOk());
    }
    @Test
    void shouldRejectRefreshTokenAfterLogout()
            throws Exception {

        String credentials = """
            {
              "email": "logout-user@example.com",
              "password": "Learning123!"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isCreated());

        MvcResult loginResult =
                mockMvc.perform(post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(credentials))
                        .andExpect(status().isOk())
                        .andReturn();

        String refreshToken = JsonPath.read(
                loginResult.getResponse()
                        .getContentAsString(),
                "$.refreshToken"
        );

        String tokenRequest = """
            {
              "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest))
                .andExpect(status().isUnauthorized());
    }
}