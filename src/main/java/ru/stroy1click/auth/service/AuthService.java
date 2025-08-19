package ru.stroy1click.auth.service;


import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.model.AuthRequest;
import ru.stroy1click.auth.model.JwtResponse;
import ru.stroy1click.auth.model.RefreshTokenRequest;
import ru.stroy1click.auth.model.User;

public interface AuthService {

    String createUser(UserDto userDto);

    String generateToken(String email);

    void logout(RefreshTokenRequest refreshTokenRequest);

    User login(AuthRequest authRequest);

    JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}
