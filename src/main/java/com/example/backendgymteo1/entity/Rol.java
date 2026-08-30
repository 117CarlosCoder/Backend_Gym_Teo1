package com.example.backendgymteo1.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rol")
public class Rol implements Serializable {

    public static final String ADMIN = "ADMIN";
    public static final String RECEPCIONISTA = "RECEPCIONISTA";
    public static final String ENTRENADOR = "ENTRENADOR";
    public static final String CLIENTE = "CLIENTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    public Rol(Integer id) {
        this.id = id;
    }

    public Rol(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    @JsonCreator
    public static Rol fromString(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return cliente();
        }
        String clean = nombre.trim().toUpperCase();
        return switch (clean) {
            case ADMIN -> admin();
            case RECEPCIONISTA -> recepcionista();
            case ENTRENADOR -> entrenador();
            case CLIENTE -> cliente();
            default -> new Rol(null, clean);
        };
    }

    @JsonValue
    public String getNombre() {
        return this.nombre != null ? this.nombre : CLIENTE;
    }

    public String name() {
        return getNombre();
    }

    public static Rol cliente() {
        return new Rol(4, CLIENTE);
    }

    public static Rol admin() {
        return new Rol(1, ADMIN);
    }

    public static Rol recepcionista() {
        return new Rol(2, RECEPCIONISTA);
    }

    public static Rol entrenador() {
        return new Rol(3, ENTRENADOR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rol rol = (Rol) o;
        return Objects.equals(id, rol.id) || (nombre != null && nombre.equalsIgnoreCase(rol.nombre));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre != null ? nombre.toUpperCase() : null);
    }
}
