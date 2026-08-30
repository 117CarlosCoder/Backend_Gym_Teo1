package com.example.backendgymteo1.service.profile;

import com.example.backendgymteo1.entity.User;

public interface RoleProfileStrategy {
    boolean soporta(String nombreRol);
    void crearPerfil(User user);
}
