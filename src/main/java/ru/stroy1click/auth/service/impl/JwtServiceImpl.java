package ru.stroy1click.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.service.JwtService;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    @Value(value = "${jwt.secret}")
    public String SECRET;

    @Override
    public String extractEmail(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    @Override
    public String generate(UserDto user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("emailConfirmed", user.getEmailConfirmed());
        return createToken(claims, user);
    }

    @Override
    public Collection<? extends GrantedAuthority> extractRole(String jwt) {
        Map<String, Object> claims = extractAllClaims(jwt);
        return Collections.singleton(new SimpleGrantedAuthority(claims.get("role").toString()));
    }

    @Override
    public boolean validate(String jwt, String originalUri) {
        try {
            Collection<? extends GrantedAuthority> authorities = extractRole(jwt);

            boolean hasRole = authorities.stream().anyMatch(auth -> {
                String role = auth.getAuthority();
                if(originalUri.startsWith("/api/v1/users") || originalUri.startsWith("/api/v1/orders")) {
                    return role.equals("ROLE_USER") || role.equals("ROLE_ADMIN");
                } else {
                    return role.equals("ROLE_ADMIN");
                }
            });

            Claims claims = extractAllClaims(jwt);
            boolean notExpired = claims.getExpiration().after(new Date());

            return hasRole && notExpired;
        } catch (JwtException e) {
            return false;
        }
    }

    private String createToken(Map<String, Object> claims, UserDto user) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 300))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
