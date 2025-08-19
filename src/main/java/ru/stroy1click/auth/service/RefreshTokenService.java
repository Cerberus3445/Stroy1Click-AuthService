package ru.stroy1click.auth.service;



import ru.stroy1click.auth.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String email);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void delete(String token);

    void deleteAll(Long userId);

}
