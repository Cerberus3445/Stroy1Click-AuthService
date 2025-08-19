package ru.stroy1click.authservice.service;

import org.springframework.security.core.GrantedAuthority;
import ru.stroy1click.authservice.dto.UserDto;
import ru.stroy1click.authservice.model.User;

import java.util.Collection;

public interface JwtService {

    String extractEmail(String token);

    String generateToken(UserDto user);

    Collection<? extends GrantedAuthority> extractRole(String token);

}
