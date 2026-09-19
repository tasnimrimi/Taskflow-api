package com.tasnim.taskflow_api;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void shouldSaveAndFindUserByEmailIgnoringCase() {
        AppUser user = new AppUser(
                "learner@example.com",
                "secure-hash"
        );

        AppUser savedUser =
                appUserRepository.saveAndFlush(user);

        Optional<AppUser> foundUser =
                appUserRepository.findByEmailIgnoreCase(
                        "LEARNER@EXAMPLE.COM"
                );

        assertNotNull(savedUser.getId());
        assertTrue(foundUser.isPresent());

        assertEquals(
                "learner@example.com",
                foundUser.get().getEmail()
        );

        assertEquals(
                "secure-hash",
                foundUser.get().getPasswordHash()
        );
    }

    @Test
    void shouldRecognizeExistingEmailIgnoringCase() {
        AppUser user = new AppUser(
                "learner@example.com",
                "secure-hash"
        );

        appUserRepository.saveAndFlush(user);

        boolean exists =
                appUserRepository.existsByEmailIgnoreCase(
                        "Learner@Example.com"
                );

        assertTrue(exists);
    }
}