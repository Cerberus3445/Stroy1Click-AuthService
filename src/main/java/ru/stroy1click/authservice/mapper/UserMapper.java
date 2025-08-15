package ru.stroy1click.authservice.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.stroy1click.authservice.dto.UserDto;
import ru.stroy1click.authservice.model.UserCredential;

@Component
@RequiredArgsConstructor
public class UserMapper implements Mappable<UserCredential, UserDto>{

    private final ModelMapper modelMapper;

    @Override
    public UserCredential toEntity(UserDto userDto) {
        return this.modelMapper.map(userDto, UserCredential.class);
    }

    @Override
    public UserDto toDto(UserCredential userCredential) {
        return this.modelMapper.map(userCredential, UserDto.class);
    }
}
