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
        return ResponseEntity.ok("The user has been created.");
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

    @DeleteMapping("/delete-all-refresh-tokens")
    public ResponseEntity<String> deleteAll(@RequestParam("userId") Long userId){
        this.refreshTokenService.deleteAll(userId);
        return ResponseEntity.ok("All tokens have been deleted.");
    }

    @DeleteMapping("/logout")
    @Operation(summary = "Logout with refreshToken")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest refreshTokenRequest){
        this.authService.logout(refreshTokenRequest);
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Create and get a refreshToken")
    public JwtResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return this.refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken())
                .map(this.refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(userCredential -> {
                    String accessToken = this.jwtService.generateToken(userCredential);
                    return JwtResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshTokenRequest.getRefreshToken())
                            .build();
                }).orElseThrow(() -> new NotFoundException("Token not found"));
    }

    private String collectErrorsToString(List<FieldError> fieldErrors){
        return fieldErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList().toString();
    }
}