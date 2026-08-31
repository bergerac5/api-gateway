package com.online.voting.gateway.security;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtUtilTests {

    private static final String SECRET_KEY = "my-super-secret-key-for-api-gateway-1234567890";

    @Test
    void shouldValidateTokenAndExtractUsernameAndRole() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", SECRET_KEY);

        String token = Jwts.builder()
                .setSubject("alice")
                .claim("role", "ADMIN")
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Claims claims = jwtUtil.validateToken(token);

        assertThat(claims.getSubject()).isEqualTo("alice");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(jwtUtil.getUsername(token)).isEqualTo("alice");
        assertThat(jwtUtil.getRole(token)).isEqualTo("ADMIN");
    }
}
