package ru.stroy1click.auth.service;


import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.model.AuthRequest;
import ru.stroy1click.auth.model.RefreshTokenRequest;
import ru.stroy1click.auth.entity.User;

public interface AuthService {

    void createUser(UserDto userDto);

    String generateToken(String email);

    void logout(RefreshTokenRequest refreshTokenRequest);

    User login(AuthRequest authRequest);
}
