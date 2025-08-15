package ru.stroy1click.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stroy1click.authservice.model.User;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
}
