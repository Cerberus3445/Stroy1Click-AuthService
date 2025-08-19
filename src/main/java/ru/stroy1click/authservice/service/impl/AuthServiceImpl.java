package ru.stroy1click.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.stroy1click.authservice.dto.UserDto;
import ru.stroy1click.authservice.exception.NotFoundException;
import ru.stroy1click.authservice.exception.ValidationException;
import ru.stroy1click.authservice.model.*;
import ru.stroy1click.authservice.service.AuthService;
import ru.stroy1click.authservice.service.JwtService;
import ru.stroy1click.authservice.service.RefreshTokenService;
import ru.stroy1click.authservice.service.UserService;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    private final ModelMapper modelMapper;

    @Override
    public String createUser(UserDto userDto) {
        userDto.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
        this.userService.create(userDto);
        return "Пользователь создан";
    }

    @Override
    public String generateToken(String email) {
        User user = this.userService.getByEmail(email).orElseThrow(
                () -> new NotFoundException(email)
        );
        UserDto userDto = this.modelMapper.map(user, UserDto.class);

        return this.jwtService.generateToken(userDto);
    }

    @Override
    public void logout(RefreshTokenRequest refreshTokenRequest) {
        this.refreshTokenService.delete(refreshTokenRequest.getRefreshToken());
    }

    @Override
    public User login(AuthRequest authRequest) {
        User user = this.userService.getByEmail(authRequest.getEmail()).orElseThrow(
                () -> new NotFoundException(authRequest.getEmail())
        );

        if(this.passwordEncoder.matches(authRequest.getPassword(), user.getPassword())){
            return user;
        } else{
            throw new ValidationException("Пароль неверен");
        }
    }

    @Override
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        RefreshToken refreshToken = this.refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken())
                .orElseThrow(() -> new NotFoundException(refreshTokenRequest));
        UserDto userDto = this.modelMapper.map(refreshToken.getUser(), UserDto.class);

        return JwtResponse.builder()
                .accessToken(this.jwtService.generateToken(userDto))
                .refreshToken(refreshToken.getToken())
                .build();
    }
}
