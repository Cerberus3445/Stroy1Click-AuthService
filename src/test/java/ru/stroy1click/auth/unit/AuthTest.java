package ru.stroy1click.auth.unit;

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
import ru.stroy1click.auth.entity.User;
import ru.stroy1click.auth.service.JwtService;
import ru.stroy1click.auth.service.RefreshTokenService;
import ru.stroy1click.auth.service.UserService;
import ru.stroy1click.auth.service.impl.AuthServiceImpl;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthTest {

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

    private User user;
    private UserDto userDto;
    private AuthRequest authRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String GENERATED_TOKEN = "generatedToken";
    private static final String REFRESH_TOKEN = "refreshToken";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        this.user = new User();
        this.user.setPassword(ENCODED_PASSWORD);

        this.userDto = new UserDto();
        this.userDto.setPassword("plainPassword");

        this.authRequest = new AuthRequest();
        this.authRequest.setEmail(TEST_EMAIL);
        this.authRequest.setPassword(TEST_PASSWORD);

        this.refreshTokenRequest = new RefreshTokenRequest();
        this.refreshTokenRequest.setRefreshToken(REFRESH_TOKEN);
    }

    @Test
    public void createUser_ShouldEncodePasswordAndCreateUser_WhenCalled() {
        // When
        this.authService.createUser(userDto);

        // Then
        verify(this.passwordEncoder).encode("plainPassword");
        verify(this.userService).create(userDto);
    }

    @Test
    public void generateToken_ShouldGenerateToken_WhenUserExists() {
        // Given
        when(this.userService.getByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(this.modelMapper.map(user, UserDto.class)).thenReturn(userDto);
        when(this.jwtService.generateToken(userDto)).thenReturn(GENERATED_TOKEN);

        // When
        String token = this.authService.generateToken(TEST_EMAIL);

        // Then
        assertEquals(GENERATED_TOKEN, token);
        verify(this.userService).getByEmail(TEST_EMAIL);
        verify(this.modelMapper).map(user, UserDto.class);
        verify(this.jwtService).generateToken(userDto);
    }

    @Test
    public void generateToken_ShouldThrowNotFoundException_WhenUserNotExists() {
        // Given
        String email = "nonexistent@example.com";
        when(this.userService.getByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> this.authService.generateToken(email));
    }

    @Test
    public void logout_ShouldDeleteRefreshToken_WhenCalled() {
        // When
        this.authService.logout(refreshTokenRequest);

        // Then
        verify(this.refreshTokenService).delete(REFRESH_TOKEN);
    }

    @Test
    public void login_ShouldReturnUser_WhenUserExistsAndPasswordMatches() {
        // Given
        when(this.userService.getByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(this.passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // When
        User result = this.authService.login(authRequest);

        // Then
        assertEquals(user, result);
    }

    @Test
    public void login_ShouldThrowNotFoundException_WhenUserNotExists() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        authRequest.setEmail(nonExistentEmail);
        when(this.userService.getByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> this.authService.login(authRequest));
    }

    @Test
    public void login_ShouldThrowValidationException_WhenPasswordDoesNotMatch() {
        // Given
        String wrongPassword = "wrongPassword";
        authRequest.setPassword(wrongPassword);
        when(this.userService.getByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(this.passwordEncoder.matches(wrongPassword, ENCODED_PASSWORD)).thenReturn(false);
        when(this.messageSource.getMessage("error.password.incorrect", null, Locale.getDefault()))
                .thenReturn("Password is incorrect");

        // When & Then
        assertThrows(ValidationException.class, () -> this.authService.login(authRequest));
    }
}
