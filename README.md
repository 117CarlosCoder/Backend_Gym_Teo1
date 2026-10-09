# Clauda Lovers - Sistema de Gestión para Gimnasios (Backend)

[![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Framework-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MariaDB](https://img.shields.io/badge/MariaDB-11-003545?style=for-the-badge&logo=mariadb&logoColor=white)](https://mariadb.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![AWS EC2](https://img.shields.io/badge/AWS-EC2%20Deployed-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white)](https://aws.amazon.com/ec2/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)

API RESTful desarrollada para la gestión administrativa, operativa y financiera de un gimnasio moderno con soporte multisucursal.

---

## Tabla de Contenidos

1. [Características Principales](#-características-principales)
2. [Arquitectura y Stack Tecnológico](#-arquitectura-y-stack-tecnológico)
3. [Despliegue en Producción (AWS EC2)](#-despliegue-en-producción-aws-ec2)
4. [Requisitos Previos](#-requisitos-previos)
5. [Variables de Entorno](#-variables-de-entorno)
6. [Guía de Ejecución Local](#-guía-de-ejecución-local)
   - [Con Docker Compose (Recomendado)](#opción-a-con-docker-compose-recomendado)
   - [Ejecución Nativa con Maven](#opción-b-ejecución-nativa-con-maven)
7. [Documentación de la API (Swagger / OpenAPI)](#-documentación-de-la-api-swagger--openapi)
8. [Usuarios de Prueba (Seeders)](#-usuarios-de-prueba-seeders)

---

## Características Principales

* **Seguridad y Control de Acceso:** Autenticación basada en tokens JWT con control de acceso por roles (`ADMIN`, `RECEPCIONISTA`, `ENTRENADOR`, `CLIENTE`).
* **Gestión Multisucursal:** Control y administración de múltiples sucursales físicas.
* **Administración de Socios y Planes:** Emisión, renovación, congelamiento y cancelación de membresías con validación de vigencias.
* **Control de Asistencias:** Registro de ingresos a instalaciones y asistencia a clases grupales con trazabilidad de sucursal y usuario registrador.
* **Caja y Facturación:** Emisión de pagos, control de estados de factura y descarga de comprobantes en formato PDF.
* **Recordatorios Automatizados:** Tareas programadas (*schedulers*) que monitorean fechas de vencimiento de membresías y envían notificaciones por correo electrónico mediante la API de Resend.

---

## Arquitectura y Stack Tecnológico

* **Lenguaje:** Java 21 (LTS).
* **Framework:** Spring Boot (Spring Web MVC, Spring Security, Spring Data JPA, Spring Validation).
* **Base de Datos:** MariaDB 11.
* **Migraciones de BD:** Flyway (migraciones automáticas versionadas `V1` a `V8`).
* **Seguridad:** Spring Security 6 + JJWT (JSON Web Token).
* **Servicio de Correo:** Resend API.
* **Documentación:** SpringDoc OpenAPI 3 / Swagger UI.
* **Contenedorización:** Docker y Docker Compose.
* **CI/CD:** GitHub Actions (compilación, pruebas unitarias y despliegue continuo).

---

## Despliegue en Producción (AWS EC2)

La aplicación se encuentra desplegada y operando en la nube sobre una instancia **AWS EC2 (Elastic Compute Cloud)**:

### Flujo de Integración y Entrega Continua (CI/CD):
1. **Integración Continua (`CI`):** Con cada `Push` o `Pull Request` a las rama `main`, un runner de GitHub Actions levanta un contenedor MariaDB 11.
2. **Entrega Continua (`CD`):** Al fusionar cambios aprobados en la rama `main`:
   - Se construye la imagen Docker optimizada de producción.
   - Se publica en **GitHub Container Registry (GHCR)** con tag versionado y `:latest`.
   - Vía SSH seguro, el workflow orquesta la actualización en la instancia **AWS EC2**, actualizando el contenedor backend sin interrumpir la persistencia de datos y reiniciando el proxy inverso Nginx.

---

## Requisitos Previos

Para ejecutar este proyecto en tu entorno local necesitas:

* **Docker & Docker Compose** (versión 20+ con Compose v2 recomendado).
* *(Opcional para desarrollo sin Docker)*:
  * **JDK 21** instalado y configurado en el `PATH`.
  * Instancia local de **MariaDB 11** en el puerto `3306`.
  * Maven 3.9+ (o utilizar el wrapper incluido `./mvnw`).

---

## Variables de Entorno

Antes de iniciar la aplicación, crea un archivo `.env` en la raíz del proyecto tomando como plantilla `.env.example`:

```bash
cp .env.example .env
```

Configura los siguientes valores según tu entorno:

| Variable | Descripción | Valor Ejemplo / Predeterminado |
| :--- | :--- | :--- |
| `DB_URL` | URL JDBC de conexión a MariaDB | `jdbc:mariadb://db:3306/gymdb` (Docker) o `localhost:3306` (Local) |
| `DB_NAME` | Nombre de la base de datos | `gymdb` |
| `DB_USER` | Usuario con permisos en la base de datos | `gymuser` |
| `DB_PASSWORD` | Contraseña del usuario de la base de datos | `gympassword123` |
| `USER_SECURITY` | Usuario de contingencia temporal en memoria | `admin` |
| `PASSWORD_SECURITY` | Contraseña para el usuario de contingencia | `admin123` |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT (mínimo 256 bits) | *Cadena secreta segura* |
| `JWT_EXPIRATION` | Tiempo de vida del token JWT en milisegundos | `86400000` (24 horas) |
| `RESEND_API_KEY` | API Key para el servicio de envío de correos Resend | `re_xxxxxxxxxxxxxx` |
| `RESEND_FROM` | Dirección y remitente de correos de notificación | `Gimnasio <onboarding@resend.dev>` |
| `DATA_INITIALIZER_ENABLED` | Ejecuta seeder inicial si la tabla de usuarios está vacía | `true` o `false` |
| `REMINDER_ENABLED` | Activa el cron de recordatorios de vencimiento | `false` (dev) / `true` (prod) |
| `REMINDER_DAYS` | Días de anticipación para avisar sobre vencimiento | `7` |
| `REMINDER_CRON` | Expresión cron para ejecución del scheduler | `0 0 9 * * *` (Todos los días a las 9 AM) |
| `REMINDER_ZONE` | Zona horaria para la evaluación del cron | `America/Guatemala` |
| `REMINDER_MAX_ATTEMPTS` | Intentos máximos de reintento de envío de correo | `3` |
| `REMINDER_RETRY_MINUTES` | Minutos de espera entre reintentos fallidos | `60` |
| `REMINDER_DISPATCHER_DELAY_MS` | Intervalo del despachador de cola en milisegundos | `60000` |

---

## Guía de Ejecución Local

### Opción A: Con Docker Compose (Recomendado)

#### 1. Entorno de Producción / Completo (App + MariaDB):
Compila la imagen y levanta tanto la base de datos MariaDB como el backend:
```bash
docker compose -f compose.yaml up --build -d
```

#### 2. Entorno de Desarrollo (Live-reload y Debug):
Monta el código en caliente y habilita el puerto de debug `5005`:
```bash
docker compose -f compose.dev.yaml up --build
```
> **Nota para Windows:** Si estás en Windows y requieres configuración específica de línea de comandos, puedes utilizar:
> ```bash
> docker compose -f compose.win.yaml up --build
> ```

#### 3. Detener los Contenedores:
```bash
docker compose down
```

---

### Opción B: Ejecución Nativa con Maven

Si prefieres correr la aplicación directamente con Java en tu máquina:

1. Asegúrate de tener MariaDB corriendo y configurada según las variables de tu archivo `.env`.
2. Compila y ejecuta las pruebas del proyecto:
   ```bash
   ./mvnw clean test
   ```
3. Inicia la aplicación Spring Boot:
   ```bash
   ./mvnw spring-boot:run
   ```

La API estará disponible en `http://localhost:8080`.

---

## Documentación de la API (Swagger / OpenAPI)

Una vez que el backend esté en ejecución, puedes explorar, probar e interactuar con todos los endpoints a través de Swagger UI:

* **Interfaz Interactiva (Swagger UI):**  
  [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui.html)

> **Cómo autenticarse en Swagger:**
> 1. Haz login en el endpoint `POST /auth/login` con uno de los usuarios de prueba.
> 2. Copia el token JWT de la respuesta.
> 3. En la parte superior de Swagger UI, presiona el botón **Authorize**.
> 4. Pega únicamente el token en el campo de texto (sin la palabra `Bearer `) y confirma.

---

## Usuarios de Prueba (Seeders)

El sistema incluye usuarios preconfigurados en las migraciones para validar los distintos flujos y permisos por rol:

| Correo Electrónico | Rol | Contraseña | Acceso y Permisos |
| :--- | :---: | :---: | :--- |
| `alejandro.garcia@gymdemo.com` | `ENTRENADOR` | `Coach123*` | Gestión de clases y asistencias deportivas |
| `maria.castro@gymdemo.com` | `RECEPCIONISTA` | `Recep123*` | Check-in en sucursal, registro de socios y pagos |
| `lucia.hernandez@gymdemo.com` | `CLIENTE` | `Client123*` | Consulta de perfil, membresía y notificaciones |
| `diego.pineda@gymdemo.com` | `CLIENTE` | `Client123*` | Consulta de estado de cuenta y asistencias |
| `valeria.mendez@gymdemo.com` | `CLIENTE` | `Client123*` | Consulta de membresía activa |

---

