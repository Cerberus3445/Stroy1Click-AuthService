package ru.stroy1click.auth;

import org.springframework.boot.SpringApplication;

public class TestStroy1ClickAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(Stroy1ClickAuthServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
