package com.tasnim.taskflow_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:taskflow-authentication"
})
@AutoConfigureMockMvc
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(
                                "learner@example.com",
                                "Learning123!"
                        )))
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

        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(
                                "learner@example.com",
                                "WrongPassword!"
                        )))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void shouldRejectUnknownEmail() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(
                                "unknown@example.com",
                                "Learning123!"
                        )))
                .andExpect(status().isUnauthorized());
    }
}