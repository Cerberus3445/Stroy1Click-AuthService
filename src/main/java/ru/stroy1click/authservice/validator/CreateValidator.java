package ru.stroy1click.authservice.validator;

import ru.stroy1click.authservice.dto.UserDto;
import ru.stroy1click.authservice.model.User;

public interface CreateValidator {

    void validate(UserDto user);
}
