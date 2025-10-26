package ru.stroy1click.auth.validator.base;

import ru.stroy1click.auth.dto.UserDto;

public interface CreateValidator<T> {

    void validate(T t);
}
