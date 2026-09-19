package com.tasnim.taskflow_api;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser registerUser(
            String email,
            String rawPassword
    ) {
        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        if (appUserRepository
                .existsByEmailIgnoreCase(normalizedEmail)) {
            return null;
        }

        String passwordHash =
                passwordEncoder.encode(rawPassword);

        AppUser user =
                new AppUser(normalizedEmail, passwordHash);

        return appUserRepository.save(user);
    }
}