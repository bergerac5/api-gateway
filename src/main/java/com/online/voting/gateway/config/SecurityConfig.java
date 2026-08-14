package com.online.voting.gateway.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

        @Value("${spring.security.oauth2.resourceserver.jwt.secret}")
        private String secretKey;

        private final Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter;

        public SecurityConfig(Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {
                this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        }

        @Bean
        public ReactiveJwtDecoder reactiveJwtDecoder() {
                SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
                return NimbusReactiveJwtDecoder.withSecretKey(key).build();
        }

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                return http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .authorizeExchange(exchange -> exchange
                                                .pathMatchers("/auth/login", "/auth/register",
                                                                "/elections/{electionId}", "/elections/bulk",
                                                                "positions/{positionId}", " /positions/bulk",
                                                                "/candidates/{candidateId}", "/candidates/bulk")
                                                .permitAll()
                                                // endpoints accessible by ADMIN ONLY on authentication and registration
                                                // services
                                                .pathMatchers("/auth/users", "/auth/users/{username}")
                                                .hasRole("ADMIN")
                                                // endpoints accessible by ADMIN only on election management
                                                .pathMatchers("/elections/createPosition",
                                                                "/elections/updateElection/{electionId}",
                                                                "/elections/deleteElection/{electionId}",
                                                                "/elections/{electionId}/status")
                                                .hasRole("ADMIN")
                                                // ADMIN only on election management endpoints
                                                .pathMatchers(
                                                                "/elections/createPosition",
                                                                "/elections/updateElection/{electionId}",
                                                                "/elections/deleteElection/{electionId}",
                                                                "/elections/{electionId}/status")
                                                .hasRole("ADMIN")

                                                .pathMatchers("/candidates/**")
                                                .hasAnyRole("ADMIN", "CANDIDATE")
                                                .anyExchange().authenticated())
                                .oauth2ResourceServer(oauth -> oauth
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter)))
                                .build();
        }
}