package com.example.backendgymteo1.mapper;

import com.example.backendgymteo1.dto.auth.AuthResponseDto;
import com.example.backendgymteo1.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AuthResponseDto toResponseDto(User user, String token) {
        if (user == null) {
            return null;
        }
        return AuthResponseDto.builder()
                .token(token)
                .correo(user.getCorreo())
                .rol(user.getRol())
                .build();
    }
}
