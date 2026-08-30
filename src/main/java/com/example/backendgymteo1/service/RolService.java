package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.role.RolResponseDto;
import com.example.backendgymteo1.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    @Transactional(readOnly = true)
    public List<RolResponseDto> findAll() {
        return rolRepository.findAll().stream()
                .map(rol -> RolResponseDto.builder()
                        .id(rol.getId())
                        .nombre(rol.getNombre())
                        .descripcion(rol.getDescripcion())
                        .build())
                .toList();
    }
}
