package ru.stroy1click.authservice.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.stroy1click.authservice.dto.UserDto;
import ru.stroy1click.authservice.exception.NotFoundException;
import ru.stroy1click.authservice.exception.ValidationException;
import ru.stroy1click.authservice.model.*;
import ru.stroy1click.authservice.service.AuthService;
import ru.stroy1click.authservice.service.JwtService;
import ru.stroy1click.authservice.service.RefreshTokenService;
import ru.stroy1click.authservice.validator.CreateValidator;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Registration, login ang getting refresh refreshToken")
@RateLimiter(name = "authLimiter")
public class AuthController {

    private final AuthService authService;

    private final RefreshTokenService refreshTokenService;

    private final JwtService jwtService;

    private final CreateValidator createValidator;

    @PostMapping("/register")
    @Operation(summary = "Create user")
    public ResponseEntity<String> registration(@RequestBody @Valid UserDto userDto, BindingResult bindingResult) {
        this.createValidator.validate(userDto);
        if(bindingResult.hasFieldErrors()) throw new ValidationException(collectErrorsToString(bindingResult.getFieldErrors()));

        userDto.setEmailConfirmed(false); //by default
        this.authService.createUser(userDto);
        return ResponseEntity.ok("Пользователь зарегистрирован.");
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public JwtResponse login(@RequestBody AuthRequest authRequest) {
        User user = this.authService.login(authRequest);
        return JwtResponse
                .builder()
                .accessToken(this.authService.generateToken(user.getEmail()))
                .refreshToken(this.refreshTokenService.createRefreshToken(user.getEmail()).getToken())
                .build();
    }

    @DeleteMapping("/logout-on-all-devices")
    @Operation(summary = "Выйти на всех устройствах.")
    public ResponseEntity<String> logoutOnAllDevices(@RequestParam("userId") Long userId){
        this.refreshTokenService.deleteAll(userId);
        return ResponseEntity.ok("Успешный выход из всех устройств.");
    }

    @DeleteMapping("/logout")
    @Operation(summary = "Выйти из сеанса(удаление только 1 refresh token.)")
    public ResponseEntity<String> logout(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest,
                                         BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()) throw new ValidationException(collectErrorsToString(bindingResult.getFieldErrors()));

        this.authService.logout(refreshTokenRequest);
        return ResponseEntity.ok("Успешный выход.");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновить access token.")
    public JwtResponse refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest,
                                    BindingResult bindingResult) {
        if(bindingResult.hasFieldErrors()) throw new ValidationException(collectErrorsToString(bindingResult.getFieldErrors()));

        return this.authService.refreshToken(refreshTokenRequest);
    }

    private String collectErrorsToString(List<FieldError> fieldErrors){
        return fieldErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList().toString();
    }
}