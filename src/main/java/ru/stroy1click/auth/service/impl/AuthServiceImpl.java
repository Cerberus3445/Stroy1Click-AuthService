package ru.stroy1click.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.entity.User;
import ru.stroy1click.auth.exception.NotFoundException;
import ru.stroy1click.auth.exception.ValidationException;
import ru.stroy1click.auth.model.*;
import ru.stroy1click.auth.service.AuthService;
import ru.stroy1click.auth.service.JwtService;
import ru.stroy1click.auth.service.RefreshTokenService;
import ru.stroy1click.auth.service.UserService;

import java.util.Locale;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    private final ModelMapper modelMapper;

    private final MessageSource messageSource;

    @Override
    public void createUser(UserDto userDto) {
        userDto.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
        this.userService.create(userDto);
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
            throw new ValidationException(
                    this.messageSource.getMessage(
                            "error.password.incorrect",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
