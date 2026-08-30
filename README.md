# Banckend Proyecto 1 Gimnasio
## Teoria de Sitemas 1
## Stack Tecnologico
Spring Boot 4

# Comandos
## dev: 
```
docker compose -f docker-compose.dev.yml up --build
```
## build:
```
docker compose up --build
```

## run:
```
docker compose up
```

## stop:
```
docker compose -f docker-compose.dev.yml down
docker-compose down
```

---

## Usuarios de Prueba

| Correo Electrónico | Rol | Contraseña |
| :--- | :---: | :---: |
| `alejandro.garcia@gymdemo.com` | `ENTRENADOR` | `Coach123*` |
| `maria.castro@gymdemo.com` | `RECEPCIONISTA` | `Recep123*` |
| `lucia.hernandez@gymdemo.com` | `CLIENTE` | `Client123*` |
| `diego.pineda@gymdemo.com` | `CLIENTE` | `Client123*` |
| `valeria.mendez@gymdemo.com` | `CLIENTE` | `Client123*` |

---

## Configuración de Variables de Entorno (`.env`)

Crea un archivo `.env` en la raíz del proyecto tomando como plantilla `.env.example`:

| Variable | Descripción | Ejemplo / Valor por defecto |
| :--- | :--- | :--- |
| `DB_URL` | URL JDBC de conexión a MariaDB | `jdbc:mariadb://localhost:3306/gymdb` (local) o `jdbc:mariadb://db:3306/gymdb` (Docker) |
| `DB_NAME` | Nombre de la base de datos | `gymdb` |
| `DB_USER` | Usuario de la base de datos | `gymuser` |
| `DB_PASSWORD` | Contraseña de la base de datos | `gympassword123` |
| `USER_SECURITY` | Usuario administrador temporal en memoria | `admin` |
| `PASSWORD_SECURITY` | Contraseña administradora temporal | `admin123` |
| `JWT_SECRET` | Clave secreta para firma de tokens JWT (mínimo 256 bits) | Hash hexadecimal seguro |
| `JWT_EXPIRATION` | Tiempo de expiración del token JWT (en milisegundos) | `86400000` (24 horas) |
| `RESEND_API_KEY` | API Key del servicio Resend para envío de correos | `re_...` |
| `RESEND_FROM` | Dirección de remitente para notificaciones por correo | `Gimnasio <onboarding@resend.dev>` |
| `DATA_INITIALIZER_ENABLED` | Activa el seeder programático de usuarios (solo si la tabla está vacía) | `true` o `false` |

---

## Puntos Clave a Tomar en Cuenta

1. **Requisitos de Java:**
   * **Java 21 LTS** obligatorio.

2. **Migraciones con Flyway:**
   * Las tablas, restricciones y catálogos se crean y actualizan de forma automática al iniciar la aplicación mediante los scripts ubicados en `src/main/resources/db/migration/`.

3. **Documentación Swagger / OpenAPI:**

    [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
    [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

