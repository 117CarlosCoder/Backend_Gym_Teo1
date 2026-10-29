# Gimnasio Backend API — Referencia de Endpoints

**Versión:** 1.0.0 · **Autenticación:** JWT Bearer (excepto los marcados como públicos) · **Base:** `http://localhost:8080`
**Documentación interactiva:** Swagger UI (`/swagger-ui/index.html`) · OpenAPI JSON (`/v3/api-docs`)

En los ejemplos de request, pegar el JWT en el header: `Authorization: Bearer <token>`. En Swagger UI, pegar **solo el token**, sin la palabra `Bearer`.

> **Nota sobre errores:** todos los errores devuelven el formato estándar `{"statusCode", "message", "error", "timestamp"}` (400, 401, 403, 404, 409, 429, 500).

---

## Tabla resumen

| Método | Ruta | Acceso | Descripción |
|:---|:---|:---|:---|
| POST | `/auth/register` | Público | Registrar usuario/miembro |
| POST | `/auth/signin` | Público | Iniciar sesión, devuelve JWT |
| POST | `/auth/signout` | Autenticado | Cerrar sesión (invalida sesión actual) |
| GET | `/roles` | Público | Catálogo de roles |
| GET | `/users` | ADMIN, RECEPCIONISTA | Listar usuarios (`?incluirEliminados`) |
| POST | `/users` | ADMIN | Crear usuario (contraseña autogenerada, notificada por correo) |
| GET | `/users/{id}` | ADMIN, RECEPCIONISTA | Buscar por ID (incluye bajas lógicas) |
| PUT | `/users/{id}` | ADMIN | Actualizar datos, rol, estado, contraseña |
| DELETE | `/users/{id}` | ADMIN | Baja lógica (libera correo, DPI y username) |
| PATCH | `/users/{id}/estado` | ADMIN | Activar o desactivar cuenta |
| GET | `/users/me` | Autenticado | Perfil propio |
| PUT | `/users/me` | Autenticado | Actualizar perfil o contraseña |
| GET | `/socios` | ADMIN, RECEPCIONISTA, ENTRENADOR | Listar con filtro de estado (paginado) |
| POST | `/socios` | ADMIN, RECEPCIONISTA | Registrar socio (contraseña autogenerada) |
| GET | `/socios/{id}` | ADMIN, RECEPCIONISTA, ENTRENADOR | Datos completos del socio |
| PUT | `/socios/{id}` | ADMIN, RECEPCIONISTA | Actualizar socio (con auditoría) |
| DELETE | `/socios/{id}` | ADMIN, RECEPCIONISTA | Baja lógica del perfil de socio |
| PATCH | `/socios/{id}/reactivar` | ADMIN, RECEPCIONISTA | Reactivar perfil de socio |
| GET | `/socios/me` | CLIENTE (socio) | Perfil de socio propio |
| GET | `/planes` | Autenticado | Catálogo de planes |
| POST | `/planes` | ADMIN | Crear plan |
| GET | `/planes/{id}` | Autenticado | Plan por ID |
| PUT | `/planes/{id}` | ADMIN | Actualizar plan |
| DELETE | `/planes/{id}` | ADMIN | Eliminar plan sin membresías asociadas |
| GET | `/membresias` | ADMIN, RECEPCIONISTA, ENTRENADOR | Listar con filtros (paginado) |
| POST | `/membresias` | ADMIN, RECEPCIONISTA | Emitir membresía (multisucursal) |
| GET | `/membresias/{id}` | ADMIN, RECEPCIONISTA, ENTRENADOR | Membresía por ID |
| PUT | `/membresias/{id}` | ADMIN, RECEPCIONISTA | Editar plan, sucursales y vencimiento |
| POST | `/membresias/{id}/cancelar` | ADMIN, RECEPCIONISTA | Cancelar con motivo y reembolso opcional |
| PATCH | `/membresias/{id}/cancelar` | ADMIN, RECEPCIONISTA | Cancelar (compatibilidad) |
| POST | `/membresias/{id}/enviar-recordatorio` | ADMIN, RECEPCIONISTA | Enviar recordatorio de vencimiento |
| GET | `/membresias/socio/{socioId}` | ADMIN, RECEPCIONISTA, ENTRENADOR | Historial de un socio |
| GET | `/membresias/proximas-a-vencer` | ADMIN, RECEPCIONISTA | Próximas a vencer (`?dias=7`) |
| GET | `/membresias/me` | CLIENTE (socio) | Historial propio |
| GET | `/membresias/me/activa` | CLIENTE (socio) | Membresía vigente propia |
| POST | `/membresias/{idMembresia}/congelamientos` | ADMIN, RECEPCIONISTA | Congelar membresía |
| GET | `/membresias/{idMembresia}/congelamientos` | ADMIN, RECEPCIONISTA | Historial de congelamientos |
| POST | `/membresias/congelamientos/{idCongelamiento}/finalizar` | ADMIN, RECEPCIONISTA | Descongelar / finalizar congelamiento |
| GET | `/membresias/me/congelamientos` | CLIENTE (socio) | Mis congelamientos |
| POST | `/asistencias/registro` | ADMIN, RECEPCIONISTA | Registrar ingreso (check-in) |
| POST | `/asistencias/{id}/salida` | ADMIN, RECEPCIONISTA | Registrar salida |
| GET | `/asistencias/{id}` | ADMIN, RECEPCIONISTA | Asistencia por ID |
| GET | `/asistencias/socio/{socioId}` | ADMIN, RECEPCIONISTA, ENTRENADOR | Historial de asistencias de un socio |
| GET | `/asistencias/hoy` | ADMIN, RECEPCIONISTA | Asistencias del día (fecha, sucursal) |
| GET | `/asistencias/me` | CLIENTE (socio) | Mis asistencias (límite 30) |
| GET | `/asistencias/metricas` | ADMIN, RECEPCIONISTA | Métricas agregadas (rango máx. 366 días) |
| POST | `/pagos` | ADMIN, RECEPCIONISTA | Registrar pago y recalcular vencimiento |
| POST | `/pagos/abonos` | ADMIN, RECEPCIONISTA | Abonar a factura pendiente |
| GET | `/pagos` | ADMIN, RECEPCIONISTA | Historial general de pagos (paginado, filtros) |
| GET | `/pagos/{id}` | Autenticado | Detalle de pago |
| GET | `/pagos/{id}/pdf` | Autenticado | Comprobante en PDF |
| GET | `/pagos/socio/{socioId}` | Autenticado | Historial paginado de pagos de un socio |
| GET | `/pagos/metodos-pago` | Autenticado | Efectivo, Tarjeta, Transferencia |
| GET | `/pagos/estado-cuenta/socio/{socioId}` | Autenticado | Estado de cuenta de un socio |
| GET | `/pagos/estado-cuenta/me` | CLIENTE (socio) | Mi estado de cuenta |
| GET | `/pagos/cotizacion/{idMembresia}` | ADMIN, RECEPCIONISTA | Cotizar pago (descuento + recargo) |
| GET | `/promociones` | ADMIN | Listar todas las promociones |
| POST | `/promociones` | ADMIN | Crear promoción |
| GET | `/promociones/vigentes` | ADMIN, RECEPCIONISTA | Promociones vigentes hoy |
| GET | `/promociones/{id}` | ADMIN | Promoción por ID |
| PUT | `/promociones/{id}` | ADMIN | Actualización parcial |
| DELETE | `/promociones/{id}` | ADMIN | Eliminar (solo si no fue usada en facturas) |
| GET | `/configuracion/recargo-tardio` | ADMIN, RECEPCIONISTA | Consultar recargo por pago tardío |
| PUT | `/configuracion/recargo-tardio` | ADMIN | Modificar recargo (0–100%) |
| GET | `/configuracion/congelamiento` | ADMIN, RECEPCIONISTA | Reglas de congelamiento |
| PUT | `/configuracion/congelamiento` | ADMIN | Modificar reglas de congelamiento |
| GET | `/configuracion/abonos` | ADMIN, RECEPCIONISTA | Reglas de pagos en abonos |
| PUT | `/configuracion/abonos` | ADMIN | Modificar reglas de abonos |
| GET | `/notificaciones/me` | Autenticado | Bandeja propia (`?leida`) |
| GET | `/notificaciones/me/no-leidas/count` | Autenticado | Conteo de no leídas |
| PATCH | `/notificaciones/me/{id}/leida` | Autenticado | Marcar como leída (idempotente) |
| GET | `/reportes/ingresos` | ADMIN | Ingresos por mes y método de pago (`?anio`) |
| GET | `/reportes/renovaciones` | ADMIN | Nuevas altas, renovaciones y tasa (`?anio`) |
| GET | `/reportes/miembros-estado` | ADMIN, RECEPCIONISTA | Activos vs. morosos (`?estado`, `?sucursalId`) |
| GET | `/reportes/dashboard` | ADMIN, RECEPCIONISTA | KPIs ejecutivos |
| GET | `/reportes/asistencias` | ADMIN, RECEPCIONISTA, CLIENTE | Métricas de asistencia según rol |

---

# 1. Autenticación (`/auth`)

## POST /auth/register — Público
Crea una nueva cuenta de usuario en el gimnasio.

**Request:**

```json
{
  "dpi": "1000000000099",
  "nombres": "Mateo Sebastián",
  "apellidos": "Morales Castillo",
  "telefono": "55550099",
  "correo": "mateo.morales@gymdemo.com",
  "contrasenia": "Client123*",
  "rol": "CLIENTE"
}
```
*Requeridos: `apellidos`, `contrasenia`, `correo`, `dpi`, `nombres`. `contrasenia` mínimo 6 caracteres; `dpi` 13–20 dígitos.*

**Response 200:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "correo": "mateo.morales@gymdemo.com",
  "rol": "CLIENTE"
}
```
*Errores: 400 (correo/DPI duplicado o campos incorrectos).*

## POST /auth/signin — Público
Autentica al usuario con correo y contraseña; retorna el token JWT.

**Request:**

```json
{
  "correo": "clp64413@gmail.com",
  "contrasenia": "Admin123*"
}
```

**Response 200:** misma estructura que `/auth/register` (`token`, `correo`, `rol`).

*Errores: 400 (datos inválidos), 401 (credenciales incorrectas), 403 (usuario no autorizado o inactivo). Rate limit: 10 peticiones/min por IP.*

## POST /auth/signout — Autenticado
Invalida la sesión actual del cliente. No requiere body.

**Response 200:**

```json
{ "message": "Sesión cerrada con éxito." }
```

---

# 2. Roles (`/roles`)

## GET /roles — Público
Retorna el catálogo completo de roles para asignación y registro.

**Response 200:**

```json
[
  { "id": 1, "nombre": "ADMIN", "descripcion": "Administrador total del sistema del gimnasio" }
]
```

---

# 3. Usuarios (`/users`)

## GET /users — ADMIN, RECEPCIONISTA
Por defecto retorna únicamente cuentas vigentes. Con `?incluirEliminados=true` incluye cuentas con baja lógica.

**Query params:** `incluirEliminados` (boolean, default `false`).

**Response 200:** array de `UserResponseDto`:

```json
{
  "id": 1,
  "dpi": "1234567890101",
  "nombres": "Juan Carlos",
  "apellidos": "Pérez López",
  "telefono": "+502 55551234",
  "direccion": "10 Calle 4-20 Zona 1",
  "fechaNacimiento": "1995-05-15",
  "correo": "juan.perez@gym.com",
  "rol": "CLIENTE",
  "estado": true,
  "contraseniaTemporal": null,
  "creadoEn": "2026-08-26T10:15:30",
  "actualizadoEn": "2026-08-26T12:30:00",
  "eliminadoEn": null
}
```
*`contraseniaTemporal` solo se incluye en la respuesta de creación.*

## POST /users — ADMIN
Crea un nuevo usuario / entrenador / miembro. La contraseña se autogenera de forma segura y se notifica por correo.

**Request:**

```json
{
  "dpi": "1000000000088",
  "nombres": "Rodrigo Fernando",
  "apellidos": "Alvarado Soto",
  "telefono": "55550088",
  "correo": "rodrigo.coach@gymdemo.com",
  "rol": "ENTRENADOR"
}
```
*Requeridos: `apellidos`, `correo`, `dpi`, `nombres`, `rol`.*

**Response 201:** `UserResponseDto` (incluye `contraseniaTemporal`, p. ej. `"aK8xP2wM4Q"`).

*Errores: 400 (datos inválidos), 403 (requiere ADMIN).*

## GET /users/{id} — ADMIN, RECEPCIONISTA
Consulta cualquier cuenta por ID, incluyendo aquellas con baja lógica (soft delete).

**Response 200:** `UserResponseDto` · *Errores: 404 (no encontrado), 403.*

## PUT /users/{id} — ADMIN
Modifica datos, rol, estado y restablece contraseña en cuentas activas o inactivas no eliminadas.

**Request (`UpdateUserAdminDto`, todos opcionales):**

```json
{
  "dpi": "1000000000002",
  "nombres": "Carlos Raúl",
  "apellidos": "López (Recepción Modificado)",
  "telefono": "55550002",
  "correo": "calin10@outlook.es",
  "contrasenia": "RecepNueva123*",
  "rol": "RECEPCIONISTA",
  "estado": true
}
```

**Response 200:** `UserResponseDto` · *Errores: 400 (datos inválidos o cuenta eliminada), 404.*

## DELETE /users/{id} — ADMIN
Baja lógica: libera correo, DPI y username (los establece en `NULL`) para permitir su reasignación. Conserva teléfono y datos históricos. Operación idempotente.

**Response:** 204 (sin contenido). · *Errores: 404, 403.*

## PATCH /users/{id}/estado — ADMIN
Desactiva (`false`) o reactiva (`true`) una cuenta no eliminada, preservando credenciales e identificadores.

**Request:**

```json
{ "estado": true }
```

**Response 200:** `UserResponseDto` · *Errores: 400 (cuenta eliminada), 404.*

## GET /users/me — Autenticado
Perfil del usuario autenticado, extraído del token JWT.

**Response 200:** `UserResponseDto`.

## PUT /users/me — Autenticado
Actualiza datos de contacto y/o cambia contraseña.

**Request (`UpdateProfileDto`, todos opcionales):**

```json
{
  "nombres": "Carlos Raúl",
  "apellidos": "López Actualizado",
  "telefono": "55550001",
  "correo": "clp64413@gmail.com",
  "contraseniaActual": "Admin123*",
  "nuevaContrasenia": "NuevoAdmin123*"
}
```
*`contraseniaActual` es requerida solo si se envía `nuevaContrasenia` (mín. 6 caracteres).*

**Response 200:** `UserResponseDto` · *Errores: 400 (contraseña actual incorrecta).*

---

# 4. Socios (`/socios`)

## GET /socios — ADMIN, RECEPCIONISTA, ENTRENADOR
Lista paginada con filtro por estado.

**Query params:** `estado` (ACTIVO | MOROSO | INACTIVO | TODOS), `page` (default 0), `size` (default 10), `sort` (ej. `id,asc`).

**Response 200 — `PageSocioResponseDto`:**

```json
{
  "content": [
    {
      "id": 4,
      "fechaRegistro": "2026-03-01",
      "estado": true,
      "estadoSocio": "ACTIVO",
      "usuario": { "id": 7, "nombres": "Carlos Raúl", "apellidos": "López", "correo": "carlos.socio@gymdemo.com", "rol": "CLIENTE", "estado": true },
      "sucursal": { "id": 1, "nombre": "Sucursal Central (Zona 1)", "direccion": "12 Calle 5-45 Zona 1", "telefono": "22340001", "activo": true },
      "membresiaActual": {
        "idMembresia": 5,
        "plan": "Plan Mensual",
        "estado": "ACTIVA",
        "fechaInicio": "2026-09-01",
        "fechaVencimiento": "2026-10-01",
        "activa": true,
        "diasRestantes": 27
      },
      "ultimaAsistencia": {
        "idAsistencia": 42,
        "fecha": "2026-09-03",
        "horaEntrada": "07:30:00",
        "horaSalida": "09:00:00",
        "recepcionista": "María Castro",
        "registradoPor": "Carlos Raúl López",
        "registradoPorRol": "ADMIN",
        "sucursal": "Sucursal Central (Zona 1)"
      },
      "nombreCompleto": "Carlos Raúl López"
    }
  ],
  "totalElements": 120, "totalPages": 12, "first": true, "last": false,
  "size": 10, "number": 0, "numberOfElements": 10, "empty": false
}
```
*`estadoSocio`: ACTIVO (membresía vigente) · MOROSO (vencida o sin membresía) · INACTIVO (dado de baja).*

## POST /socios — ADMIN, RECEPCIONISTA
Registra un nuevo socio. La contraseña se autogenera y se envía por correo.

**Request (`CreateSocioDto`):**

```json
{
  "dpi": "1000000000004",
  "nombres": "Carlos Raúl",
  "apellidos": "López",
  "telefono": "55550004",
  "direccion": "12 Calle 5-45 Zona 1",
  "fechaNacimiento": "1998-07-20",
  "correo": "carlos.socio@gymdemo.com",
  "sucursalId": 1,
  "fechaRegistro": "2026-03-01",
  "estado": "ACTIVO"
}
```
*Requeridos: `apellidos`, `correo`, `dpi`, `nombres`. `estado`: ACTIVO (default) o INACTIVO.*

**Response 201:** `SocioResponseDto` · *Errores: 400 (correo/DPI duplicado).*

## GET /socios/{id} — ADMIN, RECEPCIONISTA, ENTRENADOR
Datos completos del socio: personales, membresía actual, última asistencia y estado.

**Response 200:** `SocioResponseDto` (estructura mostrada arriba) · *Errores: 404.*

## PUT /socios/{id} — ADMIN, RECEPCIONISTA
Edición permitida: nombre, email, teléfono, dirección, fecha_nacimiento y sucursal. **No** permite editar ID ni fecha de creación. Valida email único si cambia. Registra en auditoría quién realizó el cambio y qué campos fueron modificados.

**Request (`UpdateSocioDto`, campos opcionales):**

```json
{
  "dpi": "1000000000004",
  "nombres": "Carlos Raúl",
  "apellidos": "López Modificado",
  "telefono": "55550004",
  "direccion": "12 Calle 5-45 Zona 1",
  "fechaNacimiento": "1998-07-20",
  "correo": "carlos.modificado@gymdemo.com",
  "sucursalId": 1,
  "estado": true,
  "estadoSocio": "ACTIVO"
}
```

**Response 200:** `SocioResponseDto` · *Errores: 400 (email ya en uso), 404.*

## DELETE /socios/{id} — ADMIN, RECEPCIONISTA
Baja lógica **únicamente del perfil de socio**, manteniendo intacta la cuenta de usuario y los registros históricos (pagos, asistencias, membresías). Idempotente. Registra auditoría.

**Response:** 204 · *Errores: 404.*

## PATCH /socios/{id}/reactivar — ADMIN, RECEPCIONISTA
Restaura únicamente el perfil operativo del socio. No reactiva la cuenta de usuario si estaba inactiva, ni permite reactivar si la cuenta fue eliminada.

**Response 200:** `SocioResponseDto` · *Errores: 400 (cuenta de usuario eliminada), 404.*

## GET /socios/me — CLIENTE (socio)
Perfil completo del socio autenticado (datos personales, membresía actual y última asistencia, desde el JWT).

**Response 200:** `SocioResponseDto` · *Errores: 404 (el usuario no tiene perfil de socio).*

---

# 5. Planes de Membresía (`/planes`)

## GET /planes — Autenticado
Catálogo completo de planes con duraciones y precios.

**Response 200:** array de `PlanMembresiaResponseDto`:

```json
[
  { "id": 1, "nombre": "Plan Mensual", "duracion": 30, "precio": "250.00", "descripcion": "Acceso completo a máquinas y áreas comunes" }
]
```

## POST /planes — ADMIN
Da de alta una nueva tarifa o plan. Registra auditoría.

**Request (`CreatePlanMembresiaDto`):**

```json
{
  "nombre": "Plan Trimestral Pro",
  "duracion": 90,
  "precio": "650.00",
  "descripcion": "Acceso ilimitado a todas las áreas y sucursales por 3 meses"
}
```
*Requeridos: `duracion` (≥1), `nombre`, `precio` (≥0).*

**Response 201:** `PlanMembresiaResponseDto` · *Errores: 400 (nombre duplicado).*

## GET /planes/{id} — Autenticado
**Response 200:** `PlanMembresiaResponseDto` · *Errores: 404.*

## PUT /planes/{id} — ADMIN
Actualización parcial (solo campos enviados). Registra auditoría.

**Request (`UpdatePlanMembresiaDto`):**

```json
{ "nombre": "Plan Trimestral Plus", "duracion": 90, "precio": "700.00", "descripcion": "Acceso total a sucursales y clases grupales incluidas" }
```

**Response 200:** `PlanMembresiaResponseDto` · *Errores: 400 (nombre duplicado), 404.*

## DELETE /planes/{id} — ADMIN
Elimina el plan solo si no existen membresías históricas o activas asociadas.

**Response:** 204 · *Errores: 400 (tiene membresías asociadas), 404.*

---

# 6. Membresías (`/membresias`)

## GET /membresias — ADMIN, RECEPCIONISTA, ENTRENADOR
Lista paginada con filtros avanzados.

**Query params (todos opcionales):** `socioId`, `planId`, `tipoMembresiaId`, `sucursalId`, `estadoId`, `estado` (string), `fechaDesde`, `fechaHasta` + `page`, `size`, `sort`.

**Response 200 — `PageMembresiaResponseDto`:**

```json
{
  "content": [
    {
      "id": 1,
      "socio": { "id": 4, "nombreCompleto": "Carlos Raúl López", "estadoSocio": "ACTIVO" },
      "plan": { "id": 1, "nombre": "Plan Mensual", "duracion": 30, "precio": "250.00" },
      "estado": { "id": 1, "nombre": "ACTIVA", "descripcion": "Membresía vigente" },
      "fechaInicio": "2026-03-01",
      "fechaVencimiento": "2026-03-31",
      "activa": true,
      "diasRestantes": 27,
      "sucursales": [ { "id": 1, "nombre": "Sucursal Central (Zona 1)" } ],
      "fechaCancelacion": null,
      "usuarioCancelo": null,
      "motivoCancelacion": null,
      "comentariosCancelacion": null,
      "reembolso": null,
      "montoReembolso": null
    }
  ],
  "totalElements": 85, "totalPages": 9, "first": true, "last": false,
  "size": 10, "number": 0, "numberOfElements": 10, "empty": false
}
```

## POST /membresias — ADMIN, RECEPCIONISTA
Emite una membresía multisucursal. Calcula automáticamente la fecha de vencimiento según la duración del plan.

**Request (`CreateMembresiaDto`):**

```json
{
  "idSocio": 4,
  "idPlan": 1,
  "sucursalIds": [1, 2],
  "fechaInicio": "2026-03-01",
  "idEstadoMembresia": 1
}
```
*Requeridos: `idPlan`, `idSocio`. `sucursalIds`: array de IDs (multisucursal). `fechaInicio` y `idEstadoMembresia` (default 1 = ACTIVA) son opcionales.*

**Response 201:** `MembresiaResponseDto` · *Errores: 400 (socio con membresía activa vigente), 404 (socio o plan no encontrado).*

## GET /membresias/{id} — ADMIN, RECEPCIONISTA, ENTRENADOR
**Response 200:** `MembresiaResponseDto` · *Errores: 404.*

## PUT /membresias/{id} — ADMIN, RECEPCIONISTA
Edita: tipo de membresía, sucursales (agregar/quitar) y fecha de vencimiento. **No** permite editar ID, socio ni fecha de creación. Registra auditoría.

**Request (`UpdateMembresiaDto`, campos opcionales):**

```json
{
  "tipoMembresiaId": 2,
  "sucursalIds": [1, 2, 3],
  "fechaVencimiento": "2026-11-30"
}
```

**Response 200:** `MembresiaResponseDto` · *Errores: 400 (fecha de vencimiento anterior a inicio), 404.*

## POST /membresias/{id}/cancelar — ADMIN, RECEPCIONISTA
Cancelación formal. Cambia el estado a `CANCELADA`, registra fecha, usuario que canceló, auditoría y envía email al socio.

**Request (`CancelarMembresiaRequestDto`):**

```json
{
  "motivo": "Solicitud del cliente por cambio de domicilio",
  "comentarios": "El cliente presentó constancia de mudanza y se acordó reembolso parcial.",
  "reembolso": true,
  "montoReembolso": "150.00"
}
```
*Requerido: `motivo`. `montoReembolso` requerido si `reembolso` es `true`.*

**Response 200:** `MembresiaResponseDto` (con campos de cancelación poblados) · *Errores: 400 (monto de reembolso inválido), 404.*

## PATCH /membresias/{id}/cancelar — ADMIN, RECEPCIONISTA
Compatibilidad: soporta body estructurado o `?motivo=` como query param.

**Request:** body `CancelarMembresiaRequestDto` (opcional) o `?motivo=texto`.

**Response 200:** `MembresiaResponseDto`.

## POST /membresias/{id}/enviar-recordatorio — ADMIN, RECEPCIONISTA
Envía correo de recordatorio al socio y registra el aviso en su bandeja de notificaciones (fecha de vencimiento y días restantes).

**Response 200:** objeto JSON genérico `{ "mensaje": "..." }` · *Errores: 404.*

## GET /membresias/socio/{socioId} — ADMIN, RECEPCIONISTA, ENTRENADOR
Historial completo de membresías de un socio.

**Response 200:** array de `MembresiaResponseDto` · *Errores: 404 (socio no encontrado).*

## GET /membresias/proximas-a-vencer — ADMIN, RECEPCIONISTA
Membresías activas cuya fecha de vencimiento está dentro de los próximos X días.

**Query params:** `dias` (int, default 7) + `page`, `size`, `sort`.

**Response 200:** `PageMembresiaResponseDto`.

## GET /membresias/me — CLIENTE (socio)
Historial completo de membresías del socio autenticado.

**Response 200:** array de `MembresiaResponseDto` · *Errores: 404 (sin perfil de socio).*

## GET /membresias/me/activa — CLIENTE (socio)
Membresía vigente actual del socio autenticado.

**Response 200:** `MembresiaResponseDto` · *Errores: 404 (no tiene membresía activa).*

---

# 7. Congelamiento de Membresías

## POST /membresias/{idMembresia}/congelamientos — ADMIN, RECEPCIONISTA
Congela temporalmente una membresía sumando los días congelados al vencimiento. Valida las reglas del gimnasio: días mínimos y máximos, cantidad por año, sin traslapes, membresía activa y vigente.

**Request (`CreateCongelamientoDto`):**

```json
{
  "fechaInicio": "2026-11-01",
  "fechaFin": "2026-11-15",
  "motivo": "Viaje de trabajo"
}
```
*Requerido: `fechaFin`. `fechaInicio` por defecto es hoy.*

**Response 201 — `CongelamientoResponseDto`:**

```json
{
  "id": 3,
  "idMembresia": 12,
  "nombrePlan": "Plan Mensual",
  "fechaInicio": "2026-11-01",
  "fechaFin": "2026-11-15",
  "dias": 15,
  "motivo": "Viaje de trabajo",
  "estado": "ACTIVO",
  "fechaVencimientoMembresia": "2026-12-15",
  "fechaRegistro": "2026-10-29T10:15:00",
  "registradoPor": "María Castro"
}
```
*Errores: 400 (no cumple las reglas de congelamiento), 404.*

## GET /membresias/{idMembresia}/congelamientos — ADMIN, RECEPCIONISTA
Historial de congelamientos de una membresía.

**Response 200:** array de `CongelamientoResponseDto`.

## POST /membresias/congelamientos/{idCongelamiento}/finalizar — ADMIN, RECEPCIONISTA
Descongela: termina el congelamiento hoy y descuenta del vencimiento los días no usados. Si aún no había iniciado, se anula.

**Response 200:** `CongelamientoResponseDto` · *Errores: 400 (ya terminó), 404.*

## GET /membresias/me/congelamientos — CLIENTE (socio)
Congelamientos propios para el portal del socio.

**Response 200:** array de `CongelamientoResponseDto`.

---

# 8. Asistencias (`/asistencias`)

## POST /asistencias/registro — ADMIN, RECEPCIONISTA
Check-in: valida que el socio exista, que su membresía esté activa y que la sucursal sea válida. Registra hora de entrada y recepcionista autenticado.

**Request (`RegistrarAsistenciaDto`):**

```json
{ "id_socio": 4, "sucursal_id": 1 }
```

**Response 201 — `RegistroAsistenciaResponseDto`:**

```json
{
  "mensaje": "Asistencia registrada exitosamente",
  "idAsistencia": 15,
  "fecha": "2026-09-05",
  "horaEntrada": "17:30:00",
  "registradoPorId": 1,
  "registradoPorNombre": "Carlos Raúl López",
  "registradoPorRol": "ADMIN",
  "recepcionistaId": 2,
  "recepcionistaNombre": "Carlos Raúl López",
  "sucursalId": 1,
  "sucursalNombre": "Sucursal Central (Zona 1)",
  "socio": { "id": 4, "nombreCompleto": "Carlos Raúl López", "estadoSocio": "ACTIVO" }
}
```
*Errores: 400 (membresía no activa, sucursal no activa o datos inválidos), 404 (socio o sucursal no encontrada).*

## POST /asistencias/{id}/salida — ADMIN, RECEPCIONISTA
Marca la hora de salida de una asistencia abierta. Rechaza la operación si la salida ya fue registrada.

**Response 200 — `AsistenciaDetalleDto`:**

```json
{
  "idAsistencia": 15,
  "idSocio": 8,
  "nombresSocio": "Lucía",
  "apellidosSocio": "Hernández",
  "fecha": "2026-10-21",
  "horaEntrada": "07:45:10",
  "horaSalida": "09:10:00",
  "dentro": false,
  "minutosPermanencia": 85,
  "sucursalId": 1,
  "sucursalNombre": "Sucursal Central",
  "registradoPor": "María Castro"
}
```
*Errores: 400 (salida ya registrada), 404.*

## GET /asistencias/{id} — ADMIN, RECEPCIONISTA
**Response 200:** entidad `Asistencia`:

```json
{
  "id": 101,
  "recepcionista": { "id": 2, "fechaContratacion": "2026-01-15" },
  "usuarioRegistro": { "id": 1, "nombres": "Carlos Raúl", "correo": "admin@gym.com" },
  "socio": { "id": 3, "fechaRegistro": "2026-03-01", "activo": true },
  "sucursal": { "id": 1, "nombre": "Sucursal Central", "activo": true },
  "fecha": "2026-10-15",
  "horaEntrada": "07:30:00",
  "horaSalida": "09:00:00"
}
```

## GET /asistencias/socio/{socioId} — ADMIN, RECEPCIONISTA, ENTRENADOR
Historial de asistencias de un socio.

**Response 200:** array de `Asistencia`.

## GET /asistencias/hoy — ADMIN, RECEPCIONISTA
Entradas registradas en la fecha indicada (por defecto hoy).

**Query params:** `fecha` (YYYY-MM-DD, default hoy), `sucursalId` (opcional).

**Response 200:** array de `AsistenciaDetalleDto` (incluye `dentro`: true si el socio sigue dentro).

## GET /asistencias/me — CLIENTE (socio)
Mis entradas y salidas, de la más reciente a la más antigua.

**Query params:** `limite` (default 30).

**Response 200:** array de `AsistenciaDetalleDto`.

## GET /asistencias/metricas — ADMIN, RECEPCIONISTA
Métricas agregadas en un rango de fechas (máximo 366 días): total de asistencias, socios únicos, promedio diario, serie temporal y desglose por sucursal.

**Query params (requeridos):** `desde`, `hasta` (yyyy-MM-dd) · **Opcionales:** `agrupacion` (DIA | SEMANA | MES, default DIA), `idSucursal`.

**Response 200 — `MetricasAsistenciaResponseDto`:**

```json
{
  "desde": "2026-10-01",
  "hasta": "2026-10-31",
  "agrupacion": "SEMANA",
  "idSucursal": 1,
  "totalAsistencias": 120,
  "sociosUnicos": 45,
  "promedioDiario": "3.87",
  "serie": [ { "periodo": "2026-W41", "total": 28 } ],
  "porSucursal": [ { "idSucursal": 1, "nombreSucursal": "Sucursal Central", "total": 75 } ]
}
```
*Errores: 400 (rango inválido o mayor a 366 días).*

---

# 9. Pagos (`/pagos`)

## POST /pagos — ADMIN, RECEPCIONISTA
Registra la factura y el comprobante de pago, y recalcula automáticamente la fecha de vencimiento de la membresía según la duración del plan. Si se omite la promoción, se aplica la mejor promoción vigente.

**Request (`CreatePagoDto`):**

```json
{
  "idMembresia": 1,
  "idMetodoPago": 1,
  "monto": "250.00",
  "idPromocion": 1,
  "descuento": "25.00",
  "porcentajeDescuento": "10.00",
  "referencia": "TRANS-987654",
  "observacion": "Pago realizado en caja sucursal central"
}
```
*Requeridos: `idMembresia`, `idMetodoPago`, `monto` (>0). `idMetodoPago`: 1=Efectivo, 2=Tarjeta, 3=Transferencia.*

**Response 201 — `PagoResponseDto`:**

```json
{
  "idComprobante": 100,
  "idFactura": 10,
  "idSocio": 4,
  "nombreSocio": "Juan Pérez",
  "correoSocio": "juan.perez@example.com",
  "dpiSocio": "1234567890101",
  "idMembresia": 1,
  "nombrePlan": "Plan Mensual",
  "duracionPlan": 30,
  "nuevaFechaVencimiento": "2026-11-07",
  "metodoPago": { "id": 1, "nombre": "EFECTIVO", "descripcion": "Pago en efectivo en recepción" },
  "fechaPago": "2026-10-08T15:30:00",
  "montoPagado": "250.00",
  "montoBase": "250.00",
  "descuento": "25.00",
  "recargo": "0.00",
  "idPromocion": 1,
  "nombrePromocion": "Promo Estudiantes",
  "referencia": "REF-12345",
  "nombreRecepcionista": "María López",
  "observacion": "Pago exitoso",
  "totalFactura": "250.00",
  "estadoFactura": "PENDIENTE",
  "numeroAbono": 2,
  "totalAbonado": "200.00",
  "saldoPendiente": "50.00",
  "fechaLimiteSaldo": "2026-11-13"
}
```
*Errores: 400 (monto <= 0), 404 (membresía o método de pago no encontrado).*

## POST /pagos/abonos — ADMIN, RECEPCIONISTA
Suma un comprobante a una factura PENDIENTE (pago en cuotas). El abono no puede superar el saldo; al llegar a cero la factura pasa a PAGADA.

**Request (`CreateAbonoDto`):**

```json
{ "idFactura": 15, "idMetodoPago": 1, "monto": "100.00", "referencia": "TRANS-123" }
```
*Requeridos: `idFactura`, `idMetodoPago`, `monto` (>0).*

**Response 201:** `PagoResponseDto` · *Errores: 400 (sin saldo o abono mayor al saldo), 404.*

## GET /pagos — ADMIN, RECEPCIONISTA
Historial general paginado de todos los pagos del gimnasio.

**Query params (opcionales):** `idSocio`, `fechaDesde`, `fechaHasta` (yyyy-MM-dd), `idMetodoPago` + `page`, `size`, `sort`.

**Response 200:** `PagePagoResponseDto` · *Errores: 400 (rango de fechas inválido).*

## GET /pagos/{id} — Autenticado
Detalle completo de la transacción.

**Response 200:** `PagoResponseDto` · *Errores: 404.*

## GET /pagos/{id}/pdf — Autenticado
Descarga el comprobante oficial en PDF.

**Response 200:** `application/pdf` (binario) · *Errores: 404.*

## GET /pagos/socio/{socioId} — Autenticado
Historial paginado de un socio. Un CLIENTE solo puede consultar su propio historial (403 en caso contrario).

**Query params (opcionales):** `fechaDesde`, `fechaHasta` (yyyy-MM-dd, inclusivos), `idMetodoPago` + `page`, `size`, `sort`.

**Response 200:** `PagePagoResponseDto` · *Errores: 400 (rango inválido), 403.*

## GET /pagos/metodos-pago — Autenticado
Catálogo de métodos de pago.

**Response 200:** array de `MetodoPagoResponseDto`:

```json
[ { "id": 1, "nombre": "EFECTIVO", "descripcion": "Pago en efectivo en recepción" } ]
```

## GET /pagos/estado-cuenta/socio/{socioId} — Autenticado
Facturas del socio con total, pagado, saldo pendiente, fecha límite y cada abono. Un CLIENTE solo puede consultar el suyo.

**Response 200 — `EstadoCuentaDto`:**

```json
{
  "idSocio": 4,
  "nombreSocio": "Carlos Raúl López",
  "totalFacturado": "900.00",
  "totalPagado": "750.00",
  "saldoPendiente": "150.00",
  "saldoVencido": "0.00",
  "proximaFechaLimite": "2026-11-13",
  "facturas": [
    {
      "idFactura": 15,
      "idMembresia": 12,
      "nombrePlan": "Plan Mensual",
      "fechaEmision": "2026-10-01",
      "fechaLimite": "2026-11-13",
      "montoBase": "250.00",
      "descuento": "25.00",
      "recargo": "0.00",
      "total": "225.00",
      "pagado": "225.00",
      "saldo": "0.00",
      "estado": "PAGADA",
      "abonos": [
        { "idComprobante": 101, "fechaPago": "2026-10-01T10:00:00", "monto": "125.00", "metodoPago": "EFECTIVO", "referencia": "TRANS-123" }
      ]
    }
  ]
}
```
*Estados de factura: PAGADA, PENDIENTE, VENCIDA (saldo fuera de plazo), ANULADA.*

## GET /pagos/estado-cuenta/me — CLIENTE (socio)
Estado de cuenta del socio autenticado.

**Response 200:** `EstadoCuentaDto`.

## GET /pagos/cotizacion/{idMembresia} — ADMIN, RECEPCIONISTA
Calcula el total a pagar: precio del plan − mejor descuento vigente + recargo por pago tardío si la membresía venció. El mismo cálculo se aplica al registrar el pago.

**Query params (opcionales):** `idPromocion`, `descuento`.

**Response 200 — `CotizacionPagoDto`:**

```json
{
  "idMembresia": 10,
  "montoBase": "250.00",
  "idPromocion": 1,
  "nombrePromocion": "Promo Fin de Año",
  "descuento": "25.00",
  "pagoTardio": true,
  "diasAtraso": 5,
  "porcentajeRecargo": "10.00",
  "recargo": "25.00",
  "total": "250.00",
  "fechaCalculo": "2026-10-15"
}
```
*Errores: 404 (membresía no encontrada).*

---

# 10. Promociones (`/promociones`)

## GET /promociones — ADMIN
Incluye promociones activas, inactivas y vencidas.

**Response 200:** array de `PromocionResponseDto`:

```json
{
  "id": 1,
  "nombre": "Promo Fin de Año",
  "descripcion": "10% de descuento en cualquier plan",
  "tipoDescuento": "PORCENTAJE",
  "valorDescuento": "10.00",
  "fechaInicio": "2026-12-01",
  "fechaFin": "2026-12-31",
  "activo": true,
  "vigente": true
}
```

## POST /promociones — ADMIN
Define un descuento por PORCENTAJE (0–100) o MONTO_FIJO con rango de vigencia. Registra auditoría.

**Request (`CreatePromocionDto`):**

```json
{
  "nombre": "Promo Fin de Año",
  "descripcion": "10% de descuento en cualquier plan",
  "tipoDescuento": "PORCENTAJE",
  "valorDescuento": "10.00",
  "fechaInicio": "2026-12-01",
  "fechaFin": "2026-12-31",
  "activo": true
}
```
*Requeridos: `nombre`, `tipoDescuento`, `valorDescuento` (≥0.01), `fechaInicio`, `fechaFin`.*

**Response 201:** `PromocionResponseDto` · *Errores: 400 (nombre duplicado o fechas incoherentes).*

## GET /promociones/vigentes — ADMIN, RECEPCIONISTA
Promociones activas cuyo rango de fechas incluye el día de hoy.

**Response 200:** array de `PromocionResponseDto`.

## GET /promociones/{id} — ADMIN
**Response 200:** `PromocionResponseDto` · *Errores: 404.*

## PUT /promociones/{id} — ADMIN
Actualización parcial (solo campos enviados). Registra auditoría.

**Request (`UpdatePromocionDto`, campos opcionales):**

```json
{
  "nombre": "Promo Enero",
  "descripcion": "Descuento especial de año nuevo",
  "tipoDescuento": "MONTO_FIJO",
  "valorDescuento": "50.00",
  "fechaInicio": "2026-12-01",
  "fechaFin": "2026-12-31",
  "activo": false
}
```

**Response 200:** `PromocionResponseDto` · *Errores: 400 (nombre duplicado o fechas incoherentes), 404.*

## DELETE /promociones/{id} — ADMIN
No se puede eliminar si ya fue aplicada en facturas (desactívela con PUT). Registra auditoría.

**Response:** 204 · *Errores: 400 (ya fue usada en facturas), 404.*

---

# 11. Configuración (`/configuracion`)

## GET/PUT /configuracion/recargo-tardio — GET: ADMIN, RECEPCIONISTA · PUT: ADMIN
Porcentaje aplicado sobre el precio del plan cuando la membresía ya venció al pagar. En PUT, aplica a cotizaciones y pagos nuevos; las facturas ya emitidas no cambian.

**PUT Request (`UpdateRecargoTardioDto`):**

```json
{ "porcentaje": "15.00" }
```
*Requerido: `porcentaje` (0–100).*

**Response 200 — `RecargoTardioResponseDto`:**

```json
{ "porcentaje": "10.00", "fechaActualizacion": "2026-10-15T09:50:00" }
```
*`fechaActualizacion` es null si nunca se modificó (valor por defecto). Errores PUT: 400 (fuera de rango).*

## GET/PUT /configuracion/congelamiento — GET: ADMIN, RECEPCIONISTA · PUT: ADMIN
Reglas de congelamiento. Aplica a congelamientos nuevos.

**PUT Request/Response (`ReglasCongelamientoDto`):**

```json
{
  "diasMinimo": 7,
  "diasMaximo": 30,
  "maximoPorAnio": 2
}
```
*Requeridos los tres campos. Rango: `diasMinimo`/`diasMaximo` 1–365 (mín ≤ máx); `maximoPorAnio` 1–12. La respuesta incluye `fechaActualizacion` (readOnly).*

## GET/PUT /configuracion/abonos — GET: ADMIN, RECEPCIONISTA · PUT: ADMIN
Reglas de pago en cuotas. Aplica a pagos nuevos.

**PUT Request/Response (`ReglasAbonoDto`):**

```json
{
  "porcentajeMinimoInicial": 50,
  "plazoDias": 15
}
```
*Requeridos ambos. `porcentajeMinimoInicial` 1–100 (100 = no se permiten abonos); `plazoDias` 1–90. La respuesta incluye `fechaActualizacion` (readOnly).*

---

# 12. Notificaciones (`/notificaciones`)

## GET /notificaciones/me — Autenticado
Bandeja del cliente autenticado, ordenadas cronológicamente descendente.

**Query params (opcionales):** `leida` (boolean).

**Response 200:** array de `NotificacionResponseDto`:

```json
[
  {
    "id": 1,
    "tipo": "VENCIMIENTO_MEMBRESIA",
    "titulo": "Recordatorio de Vencimiento de Membresía",
    "mensaje": "Tu membresía vencerá el 2026-10-15 (5 días restantes). Por favor acércate a recepción.",
    "fechaCreacion": "2026-10-07T08:00:00",
    "leida": false
  }
]
```

## GET /notificaciones/me/no-leidas/count — Autenticado
**Response 200:**

```json
{ "count": 3 }
```

## PATCH /notificaciones/me/{id}/leida — Autenticado
Marca como leída. Idempotente. Retorna 404 si la notificación no existe o pertenece a otro usuario.

**Response 200:** `NotificacionResponseDto` · *Errores: 404 (inexistente o ajena).*

---

# 13. Reportes y Métricas (`/reportes`)

## GET /reportes/ingresos — ADMIN
Ingresos por mes y método de pago. Año opcional (histórico si se omite).

**Query params (opcional):** `anio`.

**Response 200 — `ReporteIngresosDto`:**

```json
{
  "anio": 2026,
  "totalPeriodo": "54200.00",
  "cantidadPagosPeriodo": 215,
  "ingresoPromedioMensual": "4516.67",
  "mesMayorIngreso": "Octubre 2026",
  "montoMayorIngreso": "8500.00",
  "ingresosPorMes": [ { "anio": 2026, "mes": 10, "nombreMes": "Octubre", "total": "4500.00", "cantidadPagos": 18 } ],
  "ingresosPorMetodoPago": [ { "metodoPago": "TARJETA_CREDITO", "total": "2500.00", "cantidad": 10, "porcentaje": "55.56" } ],
  "ultimosPagos": [ { "comprobanteId": 105, "socioId": 10, "nombreSocio": "Carlos López", "planNombre": "Plan Mensual", "metodoPago": "TARJETA_CREDITO", "montoPagado": "350.00", "fechaPago": "2026-10-15T08:30:00", "numeroFactura": "FAC-2026-00105", "referencia": "AUTH-789012" } ]
}
```

## GET /reportes/renovaciones — ADMIN
Retención y renovación de membresías.

**Query params (opcional):** `anio`.

**Response 200 — `ReporteRenovacionesDto`:**

```json
{
  "anio": 2026,
  "totalMembresiasPeriodo": 350,
  "nuevasMembresias": 120,
  "totalRenovaciones": 230,
  "tasaRenovacionGlobal": "65.71",
  "proximasAVencerSiguienteSemana": 15,
  "renovacionesPorMes": [ { "anio": 2026, "mes": 10, "nombreMes": "Octubre", "nuevas": 12, "renovaciones": 28, "total": 40, "tasaRenovacion": "70.0" } ],
  "proximasAVencer": [ { "membresiaId": 42, "socioId": 10, "nombreSocio": "Carlos López", "dpi": "2987123450101", "correo": "carlos@gym.com", "telefono": "+502 5555-1234", "planNombre": "Plan Mensual", "sucursalNombre": "Central", "fechaVencimiento": "2026-10-20", "diasRestantes": 5 } ],
  "ultimasRenovaciones": [ { "membresiaId": 58, "socioId": 10, "nombreSocio": "Carlos López", "planNombre": "Plan Trimestral", "tipo": "RENOVACIÓN", "fechaInicio": "2026-10-01", "fechaVencimiento": "2026-12-31" } ]
}
```

## GET /reportes/miembros-estado — ADMIN, RECEPCIONISTA
Estadística y detalle de activos vs. morosos.

**Query params (opcionales):** `estado` (ACTIVO | MOROSO), `sucursalId`.

**Response 200 — `ReporteMiembrosEstadoDto`:**

```json
{
  "totalSocios": 250,
  "activos": 190,
  "morosos": 60,
  "porcentajeActivos": "76.0",
  "porcentajeMorosos": "24.0",
  "distribucionPorPlan": [ { "planId": 1, "nombrePlan": "Plan Anual VIP", "cantidad": 45, "porcentaje": "37.5" } ],
  "distribucionPorSucursal": [ { "sucursalId": 1, "nombreSucursal": "Sede Central", "activos": 80, "morosos": 20, "total": 100, "porcentajeSolvencia": "80.0" } ],
  "miembros": [ { "socioId": 10, "dpi": "2987123450101", "nombreCompleto": "Carlos López", "correo": "carlos@gym.com", "estado": "ACTIVO", "planNombre": "Plan Anual VIP", "fechaVencimientoMembresia": "2026-11-15", "diasRestantes": 31, "diasMora": 0, "ultimaAsistencia": "2026-10-14" } ]
}
```

## GET /reportes/dashboard — ADMIN, RECEPCIONISTA
KPIs ejecutivos. Para RECEPCIONISTA, los campos de ingresos van en `null`.

**Response 200 — `DashboardResumenDto`:**

```json
{
  "ingresosMesActual": "12500.00",
  "ingresosMesAnterior": "11000.00",
  "crecimientoIngresosPorcentaje": "13.64",
  "totalSociosActivos": 180,
  "totalSociosMorosos": 45,
  "membresiasPorVencer7Dias": 8,
  "asistenciasHoy": 65,
  "asistenciasMesActual": 1420,
  "proximosVencimientos": [ ],
  "ultimasAsistencias": [ ]
}
```

## GET /reportes/asistencias — ADMIN, RECEPCIONISTA, CLIENTE
Métricas de asistencia adaptadas según el rol autenticado.

**Query params (opcionales):** `mes` (1–12), `anio`, `sucursalId`.

**Response 200 — `MetricaAsistenciaDto`:**

```json
{
  "rol": "ADMIN",
  "asistenciasHoy": 85,
  "asistenciasSemanaActual": 420,
  "asistenciasMesActual": 1750,
  "promedioDiarioMes": "58.33",
  "sucursalId": 1,
  "nombreSucursal": "Sede Central",
  "registradasPorMiHoy": 25,
  "horasPico": [ { "hora": 18, "etiquetaHora": "18:00 - 19:00", "cantidad": 42 } ],
  "asistenciasPorDiaSemana": [ { "numeroDia": 1, "nombreDia": "Lunes", "cantidad": 120, "porcentaje": "22.5" } ],
  "asistenciasPorSucursal": [ { "sucursalId": 1, "nombreSucursal": "Sede Central", "cantidad": 350, "porcentaje": "60.0" } ],
  "topSociosFrecuentes": [ { "socioId": 5, "nombreCompleto": "Diego Pineda", "asistencias": 24 } ],
  "ultimosRegistros": [ ],
  "socioId": 4,
  "nombreSocio": "Lucia Hernandez",
  "totalVisitasHistorico": 58,
  "rachaDiasConsecutivos": 4,
  "horarioPreferido": "Mañana (05:00 - 12:00)",
  "historialVisitasPorMes": [ { "anio": 2026, "mes": 10, "nombreMes": "Octubre", "visitas": 16 } ],
  "ultimaAsistencia": { }
}
```
*(Los campos propios del socio se devuelven cuando el rol es CLIENTE.)*
