package com.online.voting.gateway.config;

import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.online.voting.gateway.handler.MissingRoleClaimException;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {

        String role = jwt.getClaim("role");

        if (role == null) {
            return Mono.error(new MissingRoleClaimException("Missing role claim in JWT"));
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        AbstractAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(authority));

        return Mono.just(auth);
    }
}