package ru.stroy1click.auth.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.exception.ValidationException;
import ru.stroy1click.auth.model.*;
import ru.stroy1click.auth.service.AuthService;
import ru.stroy1click.auth.service.RefreshTokenService;
import ru.stroy1click.auth.util.ValidationErrorUtils;
import ru.stroy1click.auth.validator.CreateValidator;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Registration, login ang getting refresh refreshToken")
@RateLimiter(name = "authLimiter")
public class AuthController {

    private final AuthService authService;

    private final RefreshTokenService refreshTokenService;

    private final CreateValidator createValidator;

    private final MessageSource messageSource;

    @PostMapping("/register")
    @Operation(summary = "Create user")
    public ResponseEntity<String> registration(@RequestBody @Valid UserDto userDto, BindingResult bindingResult) {
        this.createValidator.validate(userDto);
        if(bindingResult.hasFieldErrors()) throw new ValidationException(
                ValidationErrorUtils.collectErrorsToString(bindingResult.getFieldErrors())
        );

        userDto.setEmailConfirmed(false); //by default
        this.authService.createUser(userDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.auth.registration",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в аккаунт")
    public JwtResponse login(@RequestBody @Valid AuthRequest authRequest, BindingResult bindingResult) {
        if(bindingResult.hasFieldErrors()) throw new ValidationException(
                ValidationErrorUtils.collectErrorsToString(bindingResult.getFieldErrors())
        );

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
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.auth.logout-on-all-devices.",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/logout")
    @Operation(summary = "Выйти из сеанса(удаление только 1 refresh token.)")
    public ResponseEntity<String> logout(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest,
                                         BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()) throw new ValidationException(
                ValidationErrorUtils.collectErrorsToString(bindingResult.getFieldErrors())
        );

        this.authService.logout(refreshTokenRequest);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.auth.logout",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновить access token.")
    public JwtResponse refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest,
                                    BindingResult bindingResult) {
        if(bindingResult.hasFieldErrors()) throw new ValidationException(
                ValidationErrorUtils.collectErrorsToString(bindingResult.getFieldErrors())
        );

        return this.authService.refreshToken(refreshTokenRequest);
    }
}