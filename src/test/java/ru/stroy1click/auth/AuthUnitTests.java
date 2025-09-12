package ru.stroy1click.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.exception.NotFoundException;
import ru.stroy1click.auth.exception.ValidationException;
import ru.stroy1click.auth.model.AuthRequest;
import ru.stroy1click.auth.model.RefreshTokenRequest;
import ru.stroy1click.auth.model.User;
import ru.stroy1click.auth.service.JwtService;
import ru.stroy1click.auth.service.RefreshTokenService;
import ru.stroy1click.auth.service.UserService;
import ru.stroy1click.auth.service.impl.AuthServiceImpl;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthUnitTests {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createUser_ShouldEncodePasswordAndCreateUser() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setPassword("plainPassword");

        // When
        this.authService.createUser(userDto);

        // Then
        verify(this.passwordEncoder).encode("plainPassword");
        verify(this.userService).create(userDto);
    }

    @Test
    public void generateToken_WhenUserExists_ShouldGenerateToken() {
        // Given
        String email = "test@example.com";
        User user = new User();
        UserDto userDto = new UserDto();

        when(this.userService.getByEmail(email)).thenReturn(Optional.of(user));
        when(this.modelMapper.map(user, UserDto.class)).thenReturn(userDto);
        when(this.jwtService.generateToken(userDto)).thenReturn("generatedToken");

        // When
        String token = this.authService.generateToken(email);

        // Then
        assertEquals("generatedToken", token);
        verify(this.userService).getByEmail(email);
        verify(this.modelMapper).map(user, UserDto.class);
        verify(this.jwtService).generateToken(userDto);
    }

    @Test
    public void generateToken_WhenUserNotExists_ShouldThrowNotFoundException() {
        // Given
        String email = "nonexistent@example.com";
        when(this.userService.getByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> this.authService.generateToken(email));
    }

    @Test
    public void logout_ShouldDeleteRefreshToken() {
        // Given
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refreshToken");

        // When
        this.authService.logout(refreshTokenRequest);

        // Then
        verify(this.refreshTokenService).delete("refreshToken");
    }

    @Test
    public void login_WhenUserExistsAndPasswordMatches_ShouldReturnUser() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password");

        User user = new User();
        user.setPassword("encodedPassword");

        when(this.userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(this.passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        // When
        User result = this.authService.login(authRequest);

        // Then
        assertEquals(user, result);
    }

    @Test
    public void login_WhenUserNotExists_ShouldThrowNotFoundException() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("nonexistent@example.com");
        authRequest.setPassword("password");

        when(this.userService.getByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> this.authService.login(authRequest));
    }

    @Test
    public void login_WhenPasswordDoesNotMatch_ShouldThrowValidationException() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("wrongPassword");

        User user = new User();
        user.setPassword("encodedPassword");

        when(this.userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(this.passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);
        when(this.messageSource.getMessage("error.password.incorrect", null, Locale.getDefault()))
                .thenReturn("Password is incorrect");

        // When & Then
        assertThrows(ValidationException.class, () -> this.authService.login(authRequest));
    }
}
