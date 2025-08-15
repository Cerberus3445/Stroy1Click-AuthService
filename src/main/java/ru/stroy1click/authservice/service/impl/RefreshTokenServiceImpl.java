package ru.stroy1click.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stroy1click.authservice.exception.NotFoundException;
import ru.stroy1click.authservice.exception.ValidationException;
import ru.stroy1click.authservice.model.RefreshToken;
import ru.stroy1click.authservice.model.User;
import ru.stroy1click.authservice.repository.RefreshTokenRepository;
import ru.stroy1click.authservice.service.RefreshTokenService;
import ru.stroy1click.authservice.service.UserService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserService userService;

    /**
     * Creates a new refresh token for a user identified by their email. If the user
     * has more than 6 active sessions, throws a validation exception.
     */
    @Override
    public RefreshToken createRefreshToken(String email) {
        log.info("createRefreshToken {}", email);
        User user = this.userService.getByEmail(email).orElseThrow(
                () -> new NotFoundException("User with this email not found")
        );
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(600000))
                .build();
        if(this.refreshTokenRepository.countByUser_Id(user.getId()) <= 6){
            return this.refreshTokenRepository.save(refreshToken);
        } else {
            throw new ValidationException("The number of active sessions cannot exceed 6. Please log out on other devices.");
        }
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        log.info("findByToken {}", token);
        return this.refreshTokenRepository.findFirstByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            this.refreshTokenRepository.delete(token);
            throw new ValidationException(token.getToken() +
                    "Refresh refreshToken was expired. Please make a new signin request");
        }
        return token;
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

}
