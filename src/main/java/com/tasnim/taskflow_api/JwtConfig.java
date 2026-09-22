package com.tasnim.taskflow_api;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    @Bean
    public KeyPair jwtKeyPair() throws Exception {
        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        return generator.generateKeyPair();
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair jwtKeyPair) {
        return NimbusJwtEncoder.withKeyPair(
                (RSAPublicKey) jwtKeyPair.getPublic(),
                (RSAPrivateKey) jwtKeyPair.getPrivate()
        ).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyPair jwtKeyPair) {
        return NimbusJwtDecoder.withPublicKey(
                (RSAPublicKey) jwtKeyPair.getPublic()
        ).build();
    }
}