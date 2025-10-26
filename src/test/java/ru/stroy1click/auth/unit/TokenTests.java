package ru.stroy1click.auth.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.exception.NotFoundException;
import ru.stroy1click.auth.exception.ValidationException;
import ru.stroy1click.auth.model.JwtResponse;
import ru.stroy1click.auth.entity.RefreshToken;
import ru.stroy1click.auth.model.RefreshTokenRequest;
import ru.stroy1click.auth.entity.User;
import ru.stroy1click.auth.repository.RefreshTokenRepository;
import ru.stroy1click.auth.service.JwtService;
import ru.stroy1click.auth.service.UserService;
import ru.stroy1click.auth.service.impl.RefreshTokenServiceImpl;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenTests {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createRefreshToken_WhenUserExistsAndSessionsLessThanSix_ShouldCreateToken() {
        // Given
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);

        when(this.userService.getByEmail(email)).thenReturn(Optional.of(user));
        when(this.refreshTokenRepository.countByUser_Id(1L)).thenReturn(5);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("generated-token")
                .expiryDate(Instant.now().plusSeconds(600000))
                .build();
        
        when(this.refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // When
        RefreshToken result = this.refreshTokenService.createRefreshToken(email);

        // Then
        assertNotNull(result);
        assertEquals(user, result.getUser());
        verify(this.userService).getByEmail(email);
        verify(this.refreshTokenRepository).countByUser_Id(1L);
        verify(this.refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    public void createRefreshToken_WhenUserNotExists_ShouldThrowNotFoundException() {
        // Given
        String email = "nonexistent@example.com";
        when(this.userService.getByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> this.refreshTokenService.createRefreshToken(email));
        verify(this.userService).getByEmail(email);
    }

    @Test
    public void createRefreshToken_WhenUserHasMoreThanSixSessions_ShouldThrowValidationException() {
        // Given
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);

        when(this.userService.getByEmail(email)).thenReturn(Optional.of(user));
        when(this.refreshTokenRepository.countByUser_Id(user.getId())).thenReturn(7);
        when(this.messageSource.getMessage("error.refresh.token.max_session", null, Locale.getDefault()))
                .thenReturn("Maximum sessions exceeded");

        // When & Then
        assertThrows(ValidationException.class, () -> this.refreshTokenService.createRefreshToken(email));
        verify(this.userService).getByEmail(email);
        verify(this.refreshTokenRepository).countByUser_Id(user.getId());
    }

    @Test
    public void findByToken_WhenTokenExists_ShouldReturnToken() {
        // Given
        String token = "test-token";
        RefreshToken refreshToken = new RefreshToken();
        when(this.refreshTokenRepository.findFirstByToken(token)).thenReturn(Optional.of(refreshToken));

        // When
        Optional<RefreshToken> result = this.refreshTokenService.findByToken(token);

        // Then
        assertTrue(result.isPresent());
        assertEquals(refreshToken, result.get());
        verify(this.refreshTokenRepository).findFirstByToken(token);
    }

    @Test
    public void findByToken_WhenTokenNotExists_ShouldReturnEmptyOptional() {
        // Given
        String token = "nonexistent-token";
        when(this.refreshTokenRepository.findFirstByToken(token)).thenReturn(Optional.empty());

        // When
        Optional<RefreshToken> result = this.refreshTokenService.findByToken(token);

        // Then
        assertFalse(result.isPresent());
        verify(this.refreshTokenRepository).findFirstByToken(token);
    }

    @Test
    public void delete_ShouldDeleteTokenByTokenString() {
        // Given
        String token = "test-token";

        // When
        this.refreshTokenService.delete(token);

        // Then
        verify(this.refreshTokenRepository).deleteByToken(token);
    }

    @Test
    public void deleteAll_ShouldDeleteAllTokensForUser() {
        // Given
        Long userId = 1L;

        // When
        this.refreshTokenService.deleteAll(userId);

        // Then
        verify(this.refreshTokenRepository).deleteAllByUser_Id(userId);
    }

    @Test
    public void extendTheExpirationDate_WhenTokenExists_ShouldExtendExpiration() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("test-token");

        RefreshToken refreshToken = new RefreshToken();
        Instant oldExpiryDate = Instant.now();
        refreshToken.setExpiryDate(oldExpiryDate);

        when(this.refreshTokenRepository.findFirstByToken("test-token")).thenReturn(Optional.of(refreshToken));

        // When
        this.refreshTokenService.extendTheExpirationDate(request);

        // Then
        verify(this.refreshTokenRepository).findFirstByToken("test-token");
        verify(this.refreshTokenRepository).save(refreshToken);
        assertTrue(refreshToken.getExpiryDate().isAfter(oldExpiryDate));
    }

    @Test
    public void extendTheExpirationDate_WhenTokenNotExists_ShouldThrowNotFoundException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("nonexistent-token");

        when(this.refreshTokenRepository.findFirstByToken("nonexistent-token")).thenReturn(Optional.empty());
        when(this.messageSource.getMessage("error.refresh.token.not_found", null, Locale.getDefault()))
                .thenReturn("Refresh token not found");

        // When & Then
        assertThrows(NotFoundException.class, () -> this.refreshTokenService.extendTheExpirationDate(request));
        verify(this.refreshTokenRepository).findFirstByToken("nonexistent-token");
    }

    @Test
    public void refreshAccessToken_WhenTokenExistsAndNotExpired_ShouldReturnJwtResponse() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("test-token");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken("test-token");
        refreshToken.setExpiryDate(Instant.now().plusSeconds(600000));

        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");

        when(this.refreshTokenRepository.findFirstByToken("test-token")).thenReturn(Optional.of(refreshToken));
        when(this.modelMapper.map(refreshToken.getUser(), UserDto.class)).thenReturn(userDto);
        when(this.jwtService.generateToken(userDto)).thenReturn("new-access-token");

        // When
        JwtResponse result = this.refreshTokenService.refreshAccessToken(request);

        // Then
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("test-token", result.getRefreshToken());
        verify(this.refreshTokenRepository).findFirstByToken("test-token");
        verify(this.modelMapper).map(refreshToken.getUser(), UserDto.class);
        verify(this.jwtService).generateToken(userDto);
    }

    @Test
    public void refreshAccessToken_WhenTokenNotExists_ShouldThrowNotFoundException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("nonexistent-token");

        when(this.refreshTokenRepository.findFirstByToken("nonexistent-token")).thenReturn(Optional.empty());
        when(this.messageSource.getMessage("error.refresh.token.not_found", null, Locale.getDefault()))
                .thenReturn("Refresh token not found");

        // When & Then
        assertThrows(NotFoundException.class, () -> this.refreshTokenService.refreshAccessToken(request));
        verify(this.refreshTokenRepository).findFirstByToken("nonexistent-token");
    }

    @Test
    public void refreshAccessToken_WhenTokenExpired_ShouldThrowValidationException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        User user = new User();
        user.setId(1L);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken("expired-token");
        refreshToken.setExpiryDate(Instant.now().minusSeconds(600000)); // Expired

        when(this.refreshTokenRepository.findFirstByToken("expired-token")).thenReturn(Optional.of(refreshToken));
        when(this.messageSource.getMessage("error.refresh.token.expired", null, Locale.getDefault()))
                .thenReturn("Refresh token expired");

        // When & Then
        assertThrows(ValidationException.class, () -> this.refreshTokenService.refreshAccessToken(request));
        verify(this.refreshTokenRepository).findFirstByToken("expired-token");
    }
}
