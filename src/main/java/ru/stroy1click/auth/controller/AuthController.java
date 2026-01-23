package ru.stroy1click.auth.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.exception.ValidationException;
import ru.stroy1click.auth.model.*;
import ru.stroy1click.auth.service.AuthService;
import ru.stroy1click.auth.service.JwtService;
import ru.stroy1click.auth.service.RefreshTokenService;
import ru.stroy1click.auth.util.ValidationErrorUtils;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Регистрация, логин и получение refresh token")
@RateLimiter(name = "authLimiter")
public class AuthController {

    private final AuthService authService;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final MessageSource messageSource;

    @GetMapping("/validate")
    public ResponseEntity<Void> validateJwt(@RequestHeader(value = "Authorization", required = false) String jwt,
                                              @RequestHeader(value = "X-Original-Uri") String originalUri,
                                              @RequestHeader(value = "X-Original-Method") String originalMethod){
        if (originalMethod.equals("GET")
                && !originalUri.startsWith("/api/v1/users")
                && !originalUri.startsWith("/api/v1/orders")) {
            return ResponseEntity.ok().build();
        }

        if (jwt == null || !jwt.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwt.substring(7);
        boolean isValid = this.jwtService.validate(token, originalUri);

        if(!isValid){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/registration")
    @Operation(summary = "Зарегистрировать пользователя")
    public ResponseEntity<String> registration(@RequestBody @Valid UserDto userDto, BindingResult bindingResult) {
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

        UserDto user = this.authService.login(authRequest);

        return JwtResponse
                .builder()
                .accessToken(this.authService.generateToken(user.getEmail()))
                .refreshToken(this.refreshTokenService.createRefreshToken(user.getEmail()).getToken())
                .build();
    }

    @DeleteMapping("/logout-on-all-devices")
    @Operation(summary = "Выйти на всех устройствах.")
    public ResponseEntity<String> logoutOnAllDevices(@RequestParam("email") String email){
        this.refreshTokenService.deleteAll(email);

        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.auth.logout-on-all-devices.",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @PostMapping("/logout")
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
}