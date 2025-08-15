package ru.stroy1click.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stroy1click.authservice.dto.UserDto;
import ru.stroy1click.authservice.mapper.UserMapper;
import ru.stroy1click.authservice.model.User;
import ru.stroy1click.authservice.repository.UserCredentialRepository;
import ru.stroy1click.authservice.service.UserService;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserCredentialRepository userCredentialRepository;

    private final UserMapper mapper;

    @Override
    public void create(UserDto userDto) {
        log.info("createUser {}", userDto.getEmail());
        this.userCredentialRepository.save(this.mapper.toEntity(userDto));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getByEmail(String email) {
        log.info("getByEmail {}", email);
        return this.userCredentialRepository.findByEmail(email);
    }
}
