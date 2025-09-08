package ru.stroy1click.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.model.AuthRequest;
import ru.stroy1click.auth.model.JwtResponse;
import ru.stroy1click.auth.model.Role;

@Import({TestcontainersConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void registration(){
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("Thompson")
                .email("kate_thompson@gmail.com")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<String> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        System.out.println(responseEntity);

        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Пользователь зарегистрирован", responseEntity.getBody());
    }

    @Test
    public void login(){
        HttpEntity<AuthRequest> httpEntity = new HttpEntity<>(new AuthRequest("mike_thompson@gmail.com", "password123"));

        ResponseEntity<JwtResponse> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                httpEntity,
                JwtResponse.class
        );

        System.out.println(responseEntity);

        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(responseEntity.getBody().getAccessToken());
        Assertions.assertNotNull(responseEntity.getBody().getRefreshToken());
    }

    @Test
    public void registrationWithBlankFirstName() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("")
                .lastName("Thompson")
                .email("kate_thompson3@gmail.com")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Имя не может быть пустым"));
    }

    @Test
    public void registrationWithShortFirstName() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("K")
                .lastName("Thompson")
                .email("kate_thompson@gmail.com")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина имени составляет 2 символа, максимальная - 30 символов"));
    }

    @Test
    public void registrationWithLongFirstName() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("K".repeat(31))
                .lastName("Thompson")
                .email("kate_thompson11@gmail.com")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина имени составляет 2 символа, максимальная - 30 символов"));
    }

    @Test
    public void registrationWithBlankLastName() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("")
                .email("kate_thompson7@gmail.com")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Фамилия не может быть пустой"));
    }

    @Test
    public void registrationWithShortLastName() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("T")
                .email("kate_thompson6@gmail.com")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина фамилии составляет 2 символа, максимальная - 30 символов"));
    }

    @Test
    public void registrationWithLongLastName() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("T".repeat(31))
                .email("kate_thompson9@gmail.com")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина фамилии составляет 2 символа, максимальная - 30 символов"));
    }

    @Test
    public void registrationWithBlankEmail() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("Thompson")
                .email("")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Электронная почта не может быть пустой"));
    }

    @Test
    public void registrationWithInvalidEmail() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("Thompson")
                .email("invalid-email")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Электронная почта должна быть валидной"));
    }

    @Test
    public void registrationWithShortEmail() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("Thompson")
                .email("a@b.co")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина электронной почты составляет 8 символов, максимальная - 50 символов"));
    }

    @Test
    public void registrationWithLongEmail() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("Thompson")
                .email("a".repeat(45) + "@gmail.com")
                .password("password123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина электронной почты составляет 8 символов, максимальная - 50 символов"));
    }

    @Test
    public void registrationWithBlankPassword() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("Thompson")
                .email("kate_thompson4@gmail.com")
                .password("")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Пароль не может быть пустым"));
    }

    @Test
    public void registrationWithShortPassword() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate2")
                .lastName("Thompson2")
                .email("kate_thompson2@gmail.com")
                .password("pass123")
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина пароля составляет 8 символов, максимальная - 60 символов"));
    }

    @Test
    public void registrationWithLongPassword() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("Thompson")
                .email("kate_thompson5@gmail.com")
                .password("p".repeat(61))
                .emailConfirmed(false)
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина пароля составляет 8 символов, максимальная - 60 символов"));
    }

    @Test
    public void registrationWithNullEmailConfirmed() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate")
                .lastName("Thompson")
                .email("kate_thompson8@gmail.com")
                .password("password123")
                .role(Role.ROLE_USER)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Статус подтверждения не может быть пустым"));
    }

    @Test
    public void registrationWithNullRole() {
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(UserDto.builder()
                .firstName("Kate1")
                .lastName("Thompson1")
                .email("kate_thompson1@gmail.com")
                .password("password123")
                .emailConfirmed(false)
                .build());

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/registration",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Роль не может быть пустой"));
    }

    @Test
    public void loginWithBlankEmail() {
        HttpEntity<AuthRequest> httpEntity = new HttpEntity<>(new AuthRequest("", "password123"));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Электронная почта не может быть пустой"));
    }

    @Test
    public void loginWithInvalidEmail() {
        HttpEntity<AuthRequest> httpEntity = new HttpEntity<>(new AuthRequest("invalid-email", "password123"));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Электронная почта должна быть валидной"));
    }

    @Test
    public void loginWithShortEmail() {
        HttpEntity<AuthRequest> httpEntity = new HttpEntity<>(new AuthRequest("a@b.co", "password123"));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина электронной почты составляет 8 символов, максимальная - 50 символов"));
    }

    @Test
    public void loginWithLongEmail() {
        HttpEntity<AuthRequest> httpEntity = new HttpEntity<>(new AuthRequest("a".repeat(45) + "@gmail.com", "password123"));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина электронной почты составляет 8 символов, максимальная - 50 символов"));
    }

    @Test
    public void loginWithBlankPassword() {
        HttpEntity<AuthRequest> httpEntity = new HttpEntity<>(new AuthRequest("mike_thompson@gmail.com", ""));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Пароль не может быть пустым"));
    }

    @Test
    public void loginWithShortPassword() {
        HttpEntity<AuthRequest> httpEntity = new HttpEntity<>(new AuthRequest("mike_thompson@gmail.com", "pass123"));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина пароля составляет 8 символов, максимальная - 60 символов"));
    }

    @Test
    public void loginWithLongPassword() {
        HttpEntity<AuthRequest> httpEntity = new HttpEntity<>(new AuthRequest("mike_thompson@gmail.com", "p".repeat(61)));

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                httpEntity,
                ProblemDetail.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
        Assertions.assertTrue(responseEntity.getBody().getDetail().contains("Минимальная длина пароля составляет 8 символов, максимальная - 60 символов"));
    }

}
