package ru.stroy1click.authservice.service;


import ru.stroy1click.authservice.dto.UserDto;
import ru.stroy1click.authservice.model.AuthRequest;
import ru.stroy1click.authservice.model.RefreshTokenRequest;
import ru.stroy1click.authservice.model.UserCredential;

public interface AuthService {

    String createUser(UserDto userDto);

    String generateToken(String email);

    void logout(RefreshTokenRequest refreshToken);

    UserCredential login(AuthRequest authRequest);
}
