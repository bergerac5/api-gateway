package com.online.voting.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import com.online.voting.gateway.handler.MissingRoleClaimException;

class JwtAuthConverterTests {

    private final JwtAuthConverter converter = new JwtAuthConverter();

    @Test
    void shouldConvertJwtToAuthenticationWithRoleAuthority() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("role", "ADMIN")
                .subject("alice")
                .build();

        AbstractAuthenticationToken authentication = converter.convert(jwt).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_ADMIN");
        assertThat(authentication.getName()).isEqualTo("alice");
    }

    @Test
    void shouldFailWhenRoleClaimIsMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("alice")
                .build();

        assertThatThrownBy(() -> converter.convert(jwt).block())
                .isInstanceOf(MissingRoleClaimException.class)
                .hasMessage("Missing role claim in JWT");
    }
}
