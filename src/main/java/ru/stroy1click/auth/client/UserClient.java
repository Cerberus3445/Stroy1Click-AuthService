package ru.stroy1click.auth.client;

import ru.stroy1click.auth.dto.UserDto;

public interface UserClient {

    UserDto getByEmail(String email);

    UserDto create(UserDto userDto);
}
