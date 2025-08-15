package ru.stroy1click.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stroy1click.authservice.model.RefreshToken;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Integer> {

    Optional<RefreshToken> findFirstByToken(String token);

    void deleteByToken(String token);

    Integer countByUserCredential_Id(Long userId);

    void deleteAllByUserCredential_Id(Long userId);
}