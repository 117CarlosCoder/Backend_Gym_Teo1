package com.example.backendgymteo1.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data-initializer.enabled", havingValue = "true", matchIfMissing = false)
public class UserDataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        inicializarPlanesSiNoExisten();

        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class);
        if (userCount != null && userCount > 0) {
            log.info("[DataInitializer] La tabla 'usuario' ya contiene {} registro(s). Se omite la inicialización de usuarios.", userCount);
            return;
        }

        log.info("[DataInitializer] La tabla 'usuario' está vacía. Iniciando carga de usuarios con contraseñas hasheadas en tiempo de ejecución...");

        String adminPassword = passwordEncoder.encode("Admin123*");
        String recepPassword = passwordEncoder.encode("Recep123*");
        String coachPassword = passwordEncoder.encode("Coach123*");
        String clientPassword = passwordEncoder.encode("Client123*");

        // 1. Inserción de Usuarios base
        insertUser(1, 1, "1000000000001", "Carlos Raúl", "López (Admin)", "clp64413@gmail.com", "55550001", "clopez.admin", adminPassword);
        insertUser(2, 2, "1000000000002", "Carlos Raúl", "López (Recepción)", "calin10@outlook.es", "55550002", "calin10", recepPassword);
        insertUser(3, 3, "1000000000003", "Carlos Raúl", "López (Entrenador)", "1.carlosraullopez@gmail.com", "55550003", "carlos.coach", coachPassword);
        insertUser(4, 4, "1000000000004", "Carlos Raúl", "López (Socio CUNOC)", "carloslopez202031871@cunoc.edu.gt", "55550004", "carlos202031871", clientPassword);
        insertUser(5, 4, "1000000000005", "Carlos Raúl", "López (Socio INTECAP)", "2018-072874@intecap.edu.gt", "55550005", "carlos2018072874", clientPassword);
        insertUser(6, 3, "1000000000006", "Alejandro", "García Morales", "alejandro.garcia@gymdemo.com", "55550006", "agarcia.coach", coachPassword);
        insertUser(7, 2, "1000000000007", "María Fernanda", "Castro Silva", "maria.castro@gymdemo.com", "55550007", "mcastro.recep", recepPassword);
        insertUser(8, 4, "1000000000008", "Lucía Elena", "Hernández Ruiz", "lucia.hernandez@gymdemo.com", "55550008", "lhernandez", clientPassword);
        insertUser(9, 4, "1000000000009", "Diego Andrés", "Pineda Estrada", "diego.pineda@gymdemo.com", "55550009", "dpineda", clientPassword);
        insertUser(10, 4, "1000000000010", "Valeria Sofía", "Méndez Alvarado", "valeria.mendez@gymdemo.com", "55550010", "vmendez", clientPassword);

        // 2. Registro de Recepcionistas
        jdbcTemplate.update("INSERT INTO recepcionista (id_recepcionista, fecha_contratacion) VALUES (?, ?), (?, ?)",
                2, LocalDate.of(2024, 1, 15),
                7, LocalDate.of(2024, 2, 1));

        // 3. Registro de Entrenadores
        jdbcTemplate.update("INSERT INTO entrenador (id_entrenador, fecha_contratacion) VALUES (?, ?), (?, ?)",
                3, LocalDate.of(2024, 1, 10),
                6, LocalDate.of(2024, 2, 15));

        // 4. Registro de Socios
        jdbcTemplate.update("INSERT INTO socio (id_socio, fecha_registro, id_sucursal) VALUES (?, ?, 1), (?, ?, 1), (?, ?, 1), (?, ?, 1), (?, ?, 1)",
                4, LocalDate.of(2024, 3, 1),
                5, LocalDate.of(2024, 3, 15),
                8, LocalDate.of(2024, 4, 1),
                9, LocalDate.of(2024, 4, 10),
                10, LocalDate.of(2024, 5, 1));

        log.info("[DataInitializer] ¡Carga inicial completada exitosamente! Se crearon 10 usuarios.");
    }

    private void insertUser(int id, int idRol, String dpi, String nombres, String apellidos, String correo, String telefono,
            String username, String password) {
        jdbcTemplate.update(
                "INSERT INTO usuario (id_usuario, id_rol, dpi, nombres, apellidos, correo, telefono, username, password, activo, fecha_creacion, doble_autenticacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, 0)",
                id, idRol, dpi, nombres, apellidos, correo, telefono, username, password, LocalDateTime.now());
    }

    private void inicializarPlanesSiNoExisten() {
        try {
            Integer planCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM plan_membresia", Integer.class);
            if (planCount == null || planCount == 0) {
                jdbcTemplate.update(
                        "INSERT INTO plan_membresia (id_plan, nombre, duracion, precio, descripcion) VALUES " +
                        "(1, 'Plan Mensual', 30, 250.00, 'Acceso ilimitado por 30 días'), " +
                        "(2, 'Plan Trimestral', 90, 650.00, 'Acceso ilimitado por 3 meses con descuento'), " +
                        "(3, 'Plan Anual', 365, 2400.00, 'Acceso completo anual y evaluación nutricional') " +
                        "ON DUPLICATE KEY UPDATE nombre=VALUES(nombre)");
                log.info("[DataInitializer] Carga inicial de planes de membresía completada exitosamente.");
            }
        } catch (Exception e) {
            log.warn("[DataInitializer] No se pudo verificar/inicializar planes de membresía: {}", e.getMessage());
        }
    }
}
