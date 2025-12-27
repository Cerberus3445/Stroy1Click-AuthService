package ru.stroy1click.auth.client;

import ru.stroy1click.auth.dto.UserDto;

public interface UserClient {

    UserDto get(Long id);

    UserDto getByEmail(String email);

    void create(UserDto userDto);
}
