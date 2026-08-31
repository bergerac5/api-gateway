package com.online.voting.gateway.security;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

class JwtAuthenticationFilterTests {

    private static final String SECRET_KEY = "my-super-secret-key-for-api-gateway-1234567890";

    @Test
    void shouldAddUserHeadersWhenBearerTokenIsPresent() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        String token = Jwts.builder()
                .setSubject("alice")
                .claim("role", "ADMIN")
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        when(jwtUtil.validateToken(token)).thenReturn(claims);
        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange exchange = invocation.getArgument(0);
            assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("alice");
            assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Role")).isEqualTo("ADMIN");
            return Mono.empty();
        });

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldSkipFilterWhenAuthorizationHeaderIsMissing() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api").build());

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Id")).isNull();
        assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Role")).isNull();
    }
}
