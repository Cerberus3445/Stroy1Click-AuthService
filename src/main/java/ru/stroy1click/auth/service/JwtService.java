package ru.stroy1click.auth.service;

import org.springframework.security.core.GrantedAuthority;
import ru.stroy1click.auth.dto.UserDto;

import java.util.Collection;

public interface JwtService {

    String extractEmail(String token);

    String generateToken(UserDto user);

    Collection<? extends GrantedAuthority> extractRole(String token);

}
