package ru.stroy1click.auth.integration;

import org.springframework.boot.SpringApplication;
import ru.stroy1click.auth.Stroy1ClickAuthServiceApplication;

public class TestStroy1ClickAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(Stroy1ClickAuthServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
