package ru.stroy1click.auth.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import ru.stroy1click.auth.client.UserClient;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.exception.NotFoundException;
import ru.stroy1click.auth.exception.ValidationException;
import ru.stroy1click.auth.model.JwtResponse;
import ru.stroy1click.auth.entity.RefreshToken;
import ru.stroy1click.auth.model.RefreshTokenRequest;
import ru.stroy1click.auth.repository.RefreshTokenRepository;
import ru.stroy1click.auth.service.JwtService;
import ru.stroy1click.auth.service.impl.RefreshTokenServiceImpl;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private UserDto userDto;
    private RefreshToken refreshToken;
    private RefreshTokenRequest refreshTokenRequest;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_TOKEN = "test-token";
    private static final String GENERATED_TOKEN = "generated-token";
    private static final String NEW_ACCESS_TOKEN = "new-access-token";
    private static final Long USER_ID = 1L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        this.userDto = new UserDto();
        this.userDto.setId(USER_ID);
        this.userDto.setEmail(TEST_EMAIL);

        this.refreshToken = new RefreshToken();
        this.refreshToken.setUserId(USER_ID);
        this.refreshToken.setToken(TEST_TOKEN);
        this.refreshToken.setExpiryDate(Instant.now().plusSeconds(600000));

        this.refreshTokenRequest = new RefreshTokenRequest();
        this.refreshTokenRequest.setRefreshToken(TEST_TOKEN);
    }

    @Test
    public void createRefreshToken_ShouldCreateToken_WhenUserExistsAndSessionsLessThanSix() {
        // Given
        when(this.userClient.getByEmail(TEST_EMAIL)).thenReturn(this.userDto);
        when(this.refreshTokenRepository.countByUserId(USER_ID)).thenReturn(5);
        
        RefreshToken savedToken = RefreshToken.builder()
                .userId(USER_ID)
                .token(GENERATED_TOKEN)
                .expiryDate(Instant.now().plusSeconds(600000))
                .build();
        
        when(this.refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        // When
        RefreshToken result = this.refreshTokenService.createRefreshToken(TEST_EMAIL);

        // Then
        assertNotNull(result);
        assertEquals(this.userDto.getId(), result.getUserId());
        verify(this.userClient).getByEmail(TEST_EMAIL);
        verify(this.refreshTokenRepository).countByUserId(USER_ID);
        verify(this.refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    public void createRefreshToken_ShouldThrowNotFoundException_WhenUserNotExists() {
        // Given
        String email = "nonexistent@example.com";
        when(this.userClient.getByEmail(email)).thenThrow(new NotFoundException("User not found"));

        // When & Then
        assertThrows(NotFoundException.class, () -> this.refreshTokenService.createRefreshToken(email));
        verify(this.userClient).getByEmail(email);
    }

    @Test
    public void createRefreshToken_ShouldThrowValidationException_WhenUserHasMoreThanSixSessions() {
        // Given
        when(this.userClient.getByEmail(TEST_EMAIL)).thenReturn(this.userDto);
        when(this.refreshTokenRepository.countByUserId(USER_ID)).thenReturn(7);

        // When & Then
        assertThrows(ValidationException.class, () -> this.refreshTokenService.createRefreshToken(TEST_EMAIL));
        verify(this.userClient).getByEmail(TEST_EMAIL);
        verify(this.refreshTokenRepository).countByUserId(USER_ID);
    }

    @Test
    public void findByToken_ShouldReturnToken_WhenTokenExists() {
        // Given
        when(this.refreshTokenRepository.findFirstByToken(TEST_TOKEN)).thenReturn(Optional.of(refreshToken));

        // When
        Optional<RefreshToken> result = this.refreshTokenService.findByToken(TEST_TOKEN);

        // Then
        assertTrue(result.isPresent());
        assertEquals(refreshToken, result.get());
        verify(this.refreshTokenRepository).findFirstByToken(TEST_TOKEN);
    }

    @Test
    public void findByToken_ShouldReturnEmptyOptional_WhenTokenNotExists() {
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
    public void delete_ShouldDeleteTokenByTokenString_WhenCalled() {
        // When
        this.refreshTokenService.delete(TEST_TOKEN);

        // Then
        verify(this.refreshTokenRepository).deleteByToken(TEST_TOKEN);
    }

    @Test
    public void deleteAll_ShouldDeleteAllTokensForUser_WhenCalled() {
        // When
        this.refreshTokenService.deleteAll(USER_ID);

        // Then
        verify(this.refreshTokenRepository).deleteAllByUserId(USER_ID);
    }

    @Test
    public void extendTheExpirationDate_ShouldExtendExpiration_WhenTokenExists() {
        // Given
        Instant oldExpiryDate = Instant.now();
        refreshToken.setExpiryDate(oldExpiryDate);
        when(this.refreshTokenRepository.findFirstByToken(TEST_TOKEN)).thenReturn(Optional.of(refreshToken));

        // When
        this.refreshTokenService.extendTheExpirationDate(refreshTokenRequest);

        // Then
        verify(this.refreshTokenRepository).findFirstByToken(TEST_TOKEN);
        verify(this.refreshTokenRepository).save(refreshToken);
        assertTrue(refreshToken.getExpiryDate().isAfter(oldExpiryDate));
    }

    @Test
    public void extendTheExpirationDate_ShouldThrowNotFoundException_WhenTokenNotExists() {
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
    public void refreshAccessToken_ShouldReturnJwtResponse_WhenTokenExistsAndNotExpired() {
        // Given
        when(this.refreshTokenRepository.findFirstByToken(TEST_TOKEN)).thenReturn(Optional.of(refreshToken));
        when(this.jwtService.generateToken(userDto)).thenReturn(NEW_ACCESS_TOKEN);
        when(this.userClient.get(refreshToken.getUserId())).thenReturn(this.userDto);

        // When
        JwtResponse result = this.refreshTokenService.refreshAccessToken(this.refreshTokenRequest);

        // Then
        assertNotNull(result);
        assertEquals(NEW_ACCESS_TOKEN, result.getAccessToken());
        assertEquals(TEST_TOKEN, result.getRefreshToken());
        verify(this.refreshTokenRepository).findFirstByToken(TEST_TOKEN);
        verify(this.jwtService).generateToken(userDto);
    }

    @Test
    public void refreshAccessToken_ShouldThrowNotFoundException_WhenTokenNotExists() {
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
    public void refreshAccessToken_ShouldThrowValidationException_WhenTokenExpired() {
        // Given
        String expiredToken = "expired-token";
        RefreshToken expiredRefreshToken = new RefreshToken();
        expiredRefreshToken.setUserId(USER_ID);
        expiredRefreshToken.setToken(expiredToken);
        expiredRefreshToken.setExpiryDate(Instant.now().minusSeconds(600000)); // Expired
        
        RefreshTokenRequest expiredRequest = new RefreshTokenRequest();
        expiredRequest.setRefreshToken(expiredToken);

        when(this.refreshTokenRepository.findFirstByToken(expiredToken)).thenReturn(Optional.of(expiredRefreshToken));
        when(this.messageSource.getMessage("error.refresh.token.expired", null, Locale.getDefault()))
                .thenReturn("Refresh token expired");

        // When & Then
        assertThrows(ValidationException.class, () -> this.refreshTokenService.refreshAccessToken(expiredRequest));
        verify(this.refreshTokenRepository).findFirstByToken(expiredToken);
    }
}
