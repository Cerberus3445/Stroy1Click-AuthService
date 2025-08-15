package ru.stroy1click.authservice.service;



import ru.stroy1click.authservice.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String email);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void delete(String token);

    void deleteAll(Long userId);

}
