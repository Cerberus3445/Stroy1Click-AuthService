package ru.stroy1click.auth.service;


import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.entity.User;

import java.util.Optional;

public interface UserService {

    void create(UserDto userDto);

    Optional<User> getByEmail(String email);
}
