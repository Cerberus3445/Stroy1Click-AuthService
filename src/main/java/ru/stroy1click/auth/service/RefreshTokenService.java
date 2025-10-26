package ru.stroy1click.auth.service;



import ru.stroy1click.auth.model.JwtResponse;
import ru.stroy1click.auth.entity.RefreshToken;
import ru.stroy1click.auth.model.RefreshTokenRequest;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String email);

    Optional<RefreshToken> findByToken(String token);

    void delete(String token);

    void deleteAll(Long userId);

    void extendTheExpirationDate(RefreshTokenRequest request);

    JwtResponse refreshAccessToken(RefreshTokenRequest request);

}
