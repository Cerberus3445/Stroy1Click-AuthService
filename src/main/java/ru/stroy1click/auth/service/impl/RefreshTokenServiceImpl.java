package ru.stroy1click.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.exception.NotFoundException;
import ru.stroy1click.auth.exception.ValidationException;
import ru.stroy1click.auth.model.JwtResponse;
import ru.stroy1click.auth.model.RefreshToken;
import ru.stroy1click.auth.model.RefreshTokenRequest;
import ru.stroy1click.auth.model.User;
import ru.stroy1click.auth.repository.RefreshTokenRepository;
import ru.stroy1click.auth.service.JwtService;
import ru.stroy1click.auth.service.RefreshTokenService;
import ru.stroy1click.auth.service.UserService;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserService userService;

    private final MessageSource messageSource;

    private final ModelMapper modelMapper;

    private final JwtService jwtService;

    /**
     * Создает новый refresh токен для пользователя, идентифицируемого по email. Если у пользователя
     * более 6 активных сессий, выбрасывает исключение валидации.
     */
    @Override
    public RefreshToken createRefreshToken(String email) {
        log.info("createRefreshToken {}", email);
        User user = this.userService.getByEmail(email)
                .orElseThrow(
                        () -> new NotFoundException(email)
                );

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(600000))
                .build();

        if(this.refreshTokenRepository.countByUser_Id(user.getId()) <= 6){
            return this.refreshTokenRepository.save(refreshToken);
        } else {
            throw new ValidationException(
                    this.messageSource.getMessage(
                            "error.refresh.token.max_session",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        log.info("findByToken {}", token);
        return this.refreshTokenRepository.findFirstByToken(token);
    }

    @Override
    public void delete(String token) {
        log.info("delete {}", token);
        this.refreshTokenRepository.deleteByToken(token);
    }

    @Override
    public void deleteAll(Long userId) {
        log.info("deleteAll for user with {} id", userId);
        this.refreshTokenRepository.deleteAllByUser_Id(userId);
    }

    @Override
    public void extendTheExpirationDate(RefreshTokenRequest request) {
        RefreshToken refreshToken = this.refreshTokenRepository.findFirstByToken(request.getRefreshToken())
                        .orElseThrow(
                                () -> new NotFoundException(request)
                        );
        refreshToken.setExpiryDate(refreshToken.getExpiryDate()
                .plus(Duration.ofDays(7)));
        this.refreshTokenRepository.save(refreshToken);
    }

    @Override
    public JwtResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = this.refreshTokenRepository.findFirstByToken(request.getRefreshToken())
                .orElseThrow(
                        () -> new NotFoundException(request)
                );

        verifyExpiration(refreshToken);

        UserDto userDto = this.modelMapper.map(refreshToken.getUser(), UserDto.class);

        return JwtResponse.builder()
                .accessToken(this.jwtService.generateToken(userDto))
                .refreshToken(refreshToken.getToken())
                .build();
    }

    private void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            throw new ValidationException(
                    this.messageSource.getMessage(
                            "error.refresh.token.expired",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }

}
