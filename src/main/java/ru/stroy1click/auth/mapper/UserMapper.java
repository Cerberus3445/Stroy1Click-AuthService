package ru.stroy1click.auth.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.stroy1click.auth.dto.UserDto;
import ru.stroy1click.auth.model.User;

@Component
@RequiredArgsConstructor
public class UserMapper implements Mappable<User, UserDto>{

    private final ModelMapper modelMapper;

    @Override
    public User toEntity(UserDto userDto) {
        return this.modelMapper.map(userDto, User.class);
    }

    @Override
    public UserDto toDto(User user) {
        return this.modelMapper.map(user, UserDto.class);
    }
}
