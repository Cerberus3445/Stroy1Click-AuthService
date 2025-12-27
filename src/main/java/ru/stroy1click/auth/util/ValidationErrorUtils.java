package ru.stroy1click.auth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.validation.FieldError;
import ru.stroy1click.auth.exception.*;

import java.io.IOException;
import java.util.List;

public class ValidationErrorUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String collectErrorsToString(List<FieldError> fieldErrors){
        return fieldErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList().toString()
                .replace("[", "").replace("]", "");
    }

    public static void validateStatus(ClientHttpResponse response) throws IOException {
        String errorBody = extractErrorDetail(response);
        HttpStatus httpStatus = HttpStatus.resolve(response.getStatusCode().value());

        if (httpStatus == null)  throw new RuntimeException("Unknown HTTP status: " + response.getStatusCode().value());

        if (httpStatus.is4xxClientError()) {
            throwOnUserError(httpStatus, errorBody);
        } else {
            throwOnServerError(httpStatus);
        }
    }

    private static String extractErrorDetail(ClientHttpResponse response) {
        try {
            byte[] bodyBytes = response.getBody().readAllBytes();
            if (bodyBytes.length == 0) throw new RuntimeException("bodyBytes length is 0");

            ProblemDetail problem = objectMapper.readValue(bodyBytes, ProblemDetail.class);

            return problem.getDetail() != null ? problem.getDetail() : problem.getTitle();

        } catch (Exception e) {
            throw new RuntimeException("Error message parsing failed");
        }
    }

    private static void throwOnServerError(HttpStatus httpStatus){
        switch (httpStatus){
            case SERVICE_UNAVAILABLE -> throw new ServiceUnavailableException();
            case INTERNAL_SERVER_ERROR -> throw new ServiceErrorResponseException();
            default -> throw new RuntimeException("Unexpected status code: " + httpStatus.value());

        }
    }

    private static void throwOnUserError(HttpStatus httpStatus, String errorMessage){
        switch (httpStatus){
            case NOT_FOUND -> throw new NotFoundException(errorMessage);
            case BAD_REQUEST -> throw new ValidationException(errorMessage);
            case CONFLICT -> throw new AlreadyExistsException(errorMessage);
            default -> throw new RuntimeException("Unexpected status code: " + httpStatus.value());
        }
    }
}
