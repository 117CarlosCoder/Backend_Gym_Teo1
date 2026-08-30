package com.example.backendgymteo1.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {

    private static final String CARACTERES_PASSWORD = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int LONGITUD_POR_DEFECTO = 10;

    public String generarContraseniaAleatoria() {
        return generarContraseniaAleatoria(LONGITUD_POR_DEFECTO);
    }

    public String generarContraseniaAleatoria(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(CARACTERES_PASSWORD.charAt(RANDOM.nextInt(CARACTERES_PASSWORD.length())));
        }
        return sb.toString();
    }
}
