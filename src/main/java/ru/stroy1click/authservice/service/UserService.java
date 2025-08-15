package ru.stroy1click.authservice.service;


import ru.stroy1click.authservice.dto.UserDto;
import ru.stroy1click.authservice.model.UserCredential;

import java.util.Optional;

public interface UserService {

    void create(UserDto userDto);

    Optional<UserCredential> getByEmail(String email);
}
