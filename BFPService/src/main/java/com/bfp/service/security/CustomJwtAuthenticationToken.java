package com.bfp.service.security;

import com.bfp.auth.BFPUser;
import lombok.Getter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

@Getter
public class CustomJwtAuthenticationToken extends JwtAuthenticationToken {
    private final BFPUser user;

    public CustomJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, BFPUser user) {
        super(jwt, authorities);
        this.user = user;
    }
}