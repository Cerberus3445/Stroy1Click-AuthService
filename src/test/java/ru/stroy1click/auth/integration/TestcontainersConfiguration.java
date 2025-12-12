package ru.stroy1click.auth.integration;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    private static final GenericContainer<?> REDIS;
    private static final PostgreSQLContainer<?> POSTGRES;

    static {
        REDIS = new GenericContainer<>("redis:6.2")
                .withExposedPorts(6379);

        POSTGRES = new PostgreSQLContainer<>("postgres:15.13")
                .withInitScript("init.sql");

        REDIS.start();
        POSTGRES.start();

        System.setProperty("redisson.host", REDIS.getHost());
        System.setProperty("redisson.port", REDIS.getMappedPort(6379).toString());
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return POSTGRES;
    }

    @PreDestroy
    public void cleanup() {
        REDIS.stop();
        POSTGRES.stop();
    }

}
