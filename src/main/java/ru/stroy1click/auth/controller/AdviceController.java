package ru.stroy1click.auth.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.stroy1click.auth.exception.AlreadyExistsException;
import ru.stroy1click.auth.exception.AuthorizationException;
import ru.stroy1click.auth.exception.NotFoundException;
import ru.stroy1click.auth.exception.ValidationException;

@RestControllerAdvice
public class AdviceController {

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleException(ValidationException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage()
        );
        problemDetail.setTitle("Validation Exception");
        return problemDetail;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleException(NotFoundException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, exception.getMessage()
        );
        problemDetail.setTitle("Not found");
        return problemDetail;
    }

    @ExceptionHandler(AuthorizationException.class)
    public ProblemDetail handleException(AuthorizationException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, exception.getMessage()
        );
        problemDetail.setTitle("Authorization exception");
        problemDetail.setDetail("Incorrect password or email address");
        return problemDetail;
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ProblemDetail problemDetail(AlreadyExistsException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Already exists");
        return problemDetail;
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ProblemDetail handleException(RequestNotPermitted exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS, exception.getMessage()
        );
        problemDetail.setTitle("Too many requests");
        problemDetail.setDetail("The allowed number of requests has been exceeded. Please repeat later.");
        return problemDetail;
    }
}
