package com.example.backendgymteo1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recepcionista")
public class Recepcionista {

    @Id
    @Column(name = "id_recepcionista")
    private Integer id;

    @Column(name = "fecha_contratacion", nullable = false)
    private LocalDate fechaContratacion;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_recepcionista")
    @ToString.Exclude
    private User usuario;
}
