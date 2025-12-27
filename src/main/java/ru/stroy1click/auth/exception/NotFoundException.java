package ru.stroy1click.auth.exception;

import ru.stroy1click.auth.model.RefreshTokenRequest;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

}
