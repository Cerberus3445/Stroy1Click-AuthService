package ru.stroy1click.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(new UserDto(null,"Kate", "Thompson", "kate_thompson@gmail.com",
                "password123", false, Role.ROLE_USER));

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

}
