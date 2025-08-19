package ru.stroy1click.auth.validator;

import ru.stroy1click.auth.dto.UserDto;

public interface CreateValidator {

    void validate(UserDto user);
}
