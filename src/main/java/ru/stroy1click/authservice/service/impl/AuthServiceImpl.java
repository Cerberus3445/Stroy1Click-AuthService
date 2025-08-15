package ru.stroy1click.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.stroy1click.authservice.dto.UserDto;
import ru.stroy1click.authservice.exception.NotFoundException;
import ru.stroy1click.authservice.exception.ValidationException;
import ru.stroy1click.authservice.model.AuthRequest;
import ru.stroy1click.authservice.model.RefreshTokenRequest;
import ru.stroy1click.authservice.model.UserCredential;
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

    @Override
    public String createUser(UserDto userDto) {
        userDto.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
        this.userService.create(userDto);
        return "user added to the system";
    }

    @Override
    public String generateToken(String email) {
        UserCredential userCredential = this.userService.getByEmail(email).get();
        return this.jwtService.generateToken(userCredential);
    }

    @Override
    public void logout(RefreshTokenRequest refreshToken) {
        this.refreshTokenService.delete(refreshToken.getRefreshToken());
    }

    @Override
    public UserCredential login(AuthRequest authRequest) {
        UserCredential userCredential = this.userService.getByEmail(authRequest.getEmail()).orElseThrow(
                () -> new NotFoundException("User with this email not found")
        );

        if(this.passwordEncoder.matches(authRequest.getPassword(), userCredential.getPassword())){
            return userCredential;
        } else{
            throw new ValidationException("The password is incorrect");
        }
    }
}
