package ru.stroy1click.authservice.validator;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stroy1click.authservice.dto.UserDto;
import ru.stroy1click.authservice.exception.AlreadyExistsException;
import ru.stroy1click.authservice.service.UserService;

@Component
@RequiredArgsConstructor
public class CreateValidatorImpl implements CreateValidator{

    private final UserService userService;

    @Override
    public void validate(UserDto userDto){
        if(this.userService.getByEmail(userDto.getEmail()).isPresent()){
            throw new AlreadyExistsException("User with this email already exists");
        }
    }
}
