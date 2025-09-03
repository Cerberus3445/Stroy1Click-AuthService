package ru.stroy1click.auth.validator.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.exception.AlreadyExistsException;
import ru.stroy1click.auth.service.UserService;
import ru.stroy1click.auth.validator.CreateValidator;

@Component
@RequiredArgsConstructor
public class CreateValidatorImpl implements CreateValidator {

    private final UserService userService;

    @Override
    public void validate(UserDto userDto){
        if(this.userService.getByEmail(userDto.getEmail()).isPresent()){
            throw new AlreadyExistsException("User with this email already exists");
        }
    }
}
