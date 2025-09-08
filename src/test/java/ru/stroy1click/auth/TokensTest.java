package ru.stroy1click.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import ru.stroy1click.auth.model.JwtResponse;
import ru.stroy1click.auth.model.RefreshTokenRequest;

@Import({TestcontainersConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TokensTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void refreshAccessToken() {
        String refreshToken = "ba9a4691-ff6d-45eb-857f-1e39079ebd60";
        HttpEntity<RefreshTokenRequest> httpEntity = new HttpEntity<>(new RefreshTokenRequest(refreshToken));

        ResponseEntity<JwtResponse> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/tokens/access",
                HttpMethod.POST,
                httpEntity,
                JwtResponse.class
        );

        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(responseEntity.getBody().getAccessToken());
        Assertions.assertNotNull(responseEntity.getBody().getRefreshToken());
    }

    @Test
    public void refreshToken(){
        String refreshToken = "40f2a44f-31ed-4593-97fe-ab775e309988";
        HttpEntity<RefreshTokenRequest> httpEntity = new HttpEntity<>(new RefreshTokenRequest(refreshToken));

        ResponseEntity<String> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/tokens/refresh-token",
                HttpMethod.PATCH,
                httpEntity,
                String.class
        );

        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Refresh Token продлён", responseEntity.getBody());
    }

    @Test
    public void refreshAccessTokenWithBlankToken() {
        HttpEntity<RefreshTokenRequest> httpEntity = new HttpEntity<>(new RefreshTokenRequest(""));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/tokens/access",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Refresh token не может быть пустым"));
    }

    @Test
    public void refreshTokenWithBlankToken() {
        HttpEntity<RefreshTokenRequest> httpEntity = new HttpEntity<>(new RefreshTokenRequest(""));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/tokens/refresh-token",
                HttpMethod.PATCH,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Refresh token не может быть пустым"));
    }

    @Test
    public void refreshAccessTokenWithInvalidLengthToken() {
        HttpEntity<RefreshTokenRequest> httpEntity = new HttpEntity<>(new RefreshTokenRequest("invalid-token"));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/tokens/access",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Длина токена должна составлять 36 символов"));
    }

    @Test
    public void refreshTokenWithInvalidLengthToken() {
        HttpEntity<RefreshTokenRequest> httpEntity = new HttpEntity<>(new RefreshTokenRequest("invalid-token"));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/tokens/refresh-token",
                HttpMethod.PATCH,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Длина токена должна составлять 36 символов"));
    }
}
