# X Clone — API REST

Documentación del contrato de la API REST del proyecto **Clon de X — Proyecto Desarrollo Web**. Este documento describe los endpoints encontrados en la implementación actual de Spring Boot y distingue su comportamiento de los requisitos de referencia del curso.

## 1. Tecnologías

- Java 17
- Spring Boot
- Spring Web y Spring Data JPA
- Spring Security
- Autenticación mediante JWT
- MySQL

La base de datos configurada en desarrollo se llama `x_clone`.

## 2. URL base

Para el entorno local, los ejemplos usan:

```text
http://localhost:8080
```

## 3. Autenticación

El inicio de sesión devuelve un token JWT. Para consumir endpoints protegidos, se debe enviar el token en el encabezado HTTP:

```http
Authorization: Bearer <token>
```

El token se genera con el ID y el nombre de usuario del usuario autenticado. La duración configurada es de 24 horas.

Los endpoints públicos configurados son:

- `GET /api/users/test`
- `POST /api/users/register`
- `POST /api/users/login`

El resto de rutas requiere autenticación según la configuración actual de Spring Security.

## 4. Endpoints disponibles

### 4.1 Comprobar el controlador de usuarios

**`GET /api/users/test`**  
**Autenticación:** no requerida.

Comprueba que el controlador de usuarios responde.

**Respuesta `200 OK`** — texto plano:

```text
El controlador de usuarios funciona correctamente
```

### 4.2 Registrar usuario

**`POST /api/users/register`**  
**Autenticación:** no requerida.

**Cuerpo JSON:**

```json
{
  "name": "Santiago Salazar",
  "username": "santiago",
  "email": "santiago@example.com",
  "password": "Password123"
}
```

**Respuesta `201 Created`:**

```json
{
  "message": "Usuario registrado correctamente"
}
```

**Errores que contempla el controlador:**

- `400 Bad Request`: uno o más campos obligatorios están vacíos o ausentes.
- `409 Conflict`: el nombre de usuario ya existe.
- `409 Conflict`: el correo electrónico ya está registrado.

El servicio almacena la contraseña mediante BCrypt. La validación observada comprueba que los campos estén presentes; no se identificó en este controlador una validación de formato de correo ni una longitud mínima de contraseña.

### 4.3 Iniciar sesión

**`POST /api/users/login`**  
**Autenticación:** no requerida.

La implementación actual inicia sesión mediante **correo electrónico y contraseña**.

**Cuerpo JSON:**

```json
{
  "email": "santiago@example.com",
  "password": "Password123"
}
```

**Respuesta `200 OK`:**

```json
{
  "message": "Inicio de sesión correcto",
  "token": "<jwt>",
  "id": "1",
  "name": "Santiago Salazar",
  "username": "santiago",
  "email": "santiago@example.com"
}
```

El token y los datos de usuario son valores de ejemplo; la API devuelve los valores correspondientes a la cuenta autenticada. El campo `id` se serializa como texto en esta respuesta.

**Errores que contempla el controlador:**

- `400 Bad Request`: falta el correo electrónico o la contraseña.
- `401 Unauthorized`: las credenciales no son correctas.

### 4.4 Consultar un perfil

**`GET /api/users/{username}`**  
**Autenticación:** requerida.

Ejemplo:

```http
GET /api/users/santiago
Authorization: Bearer <token>
```

**Respuesta `200 OK`:**

```json
{
  "id": "1",
  "username": "santiago",
  "name": "Santiago Salazar"
}
```

**Respuesta `404 Not Found`:**

```json
{
  "message": "Usuario no encontrado"
}
```

El perfil devuelve actualmente `id`, `username` y `name`. No incluye contadores de seguidores/seguidos ni el campo `isFollowing`.

### 4.5 Actualizar el nombre del perfil propio

**`PUT /api/users/{username}`**  
**Autenticación:** requerida.

Solo permite actualizar el perfil correspondiente al usuario autenticado.

**Cuerpo JSON:**

```json
{
  "name": "Santiago S. Salazar"
}
```

**Respuesta `200 OK`:**

```json
{
  "id": "1",
  "username": "santiago",
  "name": "Santiago S. Salazar"
}
```

**Errores que contempla el controlador:**

- `400 Bad Request`: el nombre está vacío.
- `401 Unauthorized`: no se pudo identificar al usuario autenticado.
- `403 Forbidden`: se intenta modificar el perfil de otra persona.
- `404 Not Found`: el usuario indicado no existe.

### 4.6 Crear una publicación

**`POST /api/posts`**  
**Autenticación:** requerida.

El autor se determina a partir del token; no se envía el ID del autor en el cuerpo.

**Cuerpo JSON:**

```json
{
  "content": "Mi primera publicación en X Clone"
}
```

**Respuesta `201 Created`** — estructura:

```json
{
  "id": 1,
  "content": "Mi primera publicación en X Clone",
  "createdAt": "2026-10-28T15:30:00",
  "user": {
    "id": 1,
    "username": "santiago",
    "name": "Santiago Salazar"
  }
}
```

Los valores de la respuesta son ilustrativos. `createdAt` corresponde a la fecha y hora guardadas por el backend.

**Errores que contempla el controlador/servicio:**

- `400 Bad Request`: el contenido está vacío o supera los 280 caracteres.
- `401 Unauthorized`: no se identificó al usuario autenticado.

### 4.7 Consultar todas las publicaciones

**`GET /api/posts`**  
**Autenticación:** requerida.

Devuelve una lista JSON de publicaciones, ordenadas de la más reciente a la más antigua.

**Respuesta `200 OK`** — estructura:

```json
[
  {
    "id": 1,
    "content": "Mi primera publicación",
    "createdAt": "2026-10-28T15:30:00",
    "user": {
      "id": 1,
      "username": "santiago",
      "name": "Santiago Salazar"
    }
  }
]
```

Cada elemento incluye `id`, `content`, `createdAt` y un objeto `user` con `id`, `username` y `name`.

**Comportamiento actual:** devuelve todas las publicaciones. No recibe `page` ni `size`, y no filtra por los usuarios que sigue el usuario autenticado.

### 4.8 Consultar publicaciones de un usuario

**`GET /api/posts/user/{username}`**  
**Autenticación:** requerida.

Ejemplo:

```http
GET /api/posts/user/santiago
Authorization: Bearer <token>
```

Devuelve una lista de publicaciones del usuario indicado, de la más reciente a la más antigua. Los objetos tienen la misma estructura que en `GET /api/posts`.

**Comportamiento actual:** no implementa paginación. Si el usuario no tiene publicaciones o no hay coincidencias, la consulta puede devolver una lista vacía.

### 4.9 Seguir a un usuario

**`POST /api/follows/{username}`**  
**Autenticación:** requerida.

El nombre de usuario de la ruta identifica a la persona que se desea seguir. No requiere cuerpo JSON.

Ejemplo:

```http
POST /api/follows/camila
Authorization: Bearer <token>
```

**Respuesta `201 Created`:**

```json
{
  "message": "Ahora sigues a camila",
  "createdAt": "2026-10-28T15:35:00"
}
```

**Errores que contempla el controlador/servicio:**

- `400 Bad Request`: el usuario no existe, se intenta seguirse a sí mismo o ya existe el seguimiento.
- `401 Unauthorized`: no se identificó al usuario autenticado.

La entidad de seguimiento establece una restricción única para la pareja seguidor/seguido.

### 4.10 Consultar el número de seguidores

**`GET /api/follows/{username}/followers/count`**  
**Autenticación:** requerida.

**Respuesta `200 OK`:**

```json
{
  "count": 12
}
```

El número `12` es solo un ejemplo. Si el usuario no existe, el controlador devuelve `404 Not Found` con un objeto JSON vacío (`{}`).

### 4.11 Consultar el número de seguidos

**`GET /api/follows/{username}/following/count`**  
**Autenticación:** requerida.

**Respuesta `200 OK`:**

```json
{
  "count": 8
}
```

El número `8` es solo un ejemplo. Si el usuario no existe, el controlador devuelve `404 Not Found` con un objeto JSON vacío (`{}`).

### 4.12 Comprobar si se sigue a un usuario

**`GET /api/follows/{username}/is-following`**  
**Autenticación:** requerida.

**Respuesta `200 OK`:**

```json
{
  "isFollowing": true
}
```

El valor booleano depende de la relación entre el usuario autenticado y el usuario indicado en la ruta.

Si el controlador no puede obtener el ID del usuario autenticado, devuelve `401 Unauthorized` con:

```json
{
  "isFollowing": false
}
```

## 5. Modelo de datos

Los modelos se representan mediante entidades JPA.

### User

| Campo Java | Descripción |
|---|---|
| `id` | Identificador generado automáticamente. |
| `username` | Nombre de usuario. |
| `name` | Nombre mostrado del usuario. |
| `email` | Correo electrónico. |
| `password` | Contraseña almacenada como hash BCrypt por el servicio de registro. |

### Post

La entidad `Post` utiliza la tabla `posts`.

| Campo Java | Descripción |
|---|---|
| `id` | Identificador generado automáticamente. |
| `content` | Contenido de la publicación, máximo 280 caracteres. |
| `createdAt` | Fecha y hora de creación (`LocalDateTime`). |
| `user` | Relación con el usuario autor; columna de relación `user_id`. |

### Follow

La entidad `Follow` utiliza la tabla `follows`.

| Campo Java | Descripción |
|---|---|
| `id` | Identificador generado automáticamente. |
| `follower` | Usuario que realiza el seguimiento; columna `follower_id`. |
| `followed` | Usuario seguido; columna `followed_id`. |
| `createdAt` | Fecha y hora del seguimiento. |

La pareja `follower_id` / `followed_id` tiene una restricción única.

## 6. Configuración relevante

- La conexión JDBC de desarrollo apunta a `jdbc:mysql://localhost:3306/x_clone`.
- Hibernate está configurado con `spring.jpa.hibernate.ddl-auto=update`.
- CORS permite actualmente el origen `http://localhost:5173`.
- El JWT está configurado mediante `JWT_SECRET` y tiene una expiración de 24 horas.
- Las credenciales y secretos deben suministrarse de forma segura mediante variables de entorno y no incluirse en el repositorio.

**Nota de configuración:** antes de ejecutar desde otro puerto de Vite, verifica que el origen real del frontend esté permitido por CORS. El frontend utiliza `VITE_API_URL` en algunas peticiones, pero también contiene URLs de `localhost:8080` escritas directamente en ciertas páginas; conviene unificar esa configuración como mejora separada.

## 7. Relación con los requisitos del proyecto

El documento del curso presenta rutas de referencia, pero permite cambiar sus nombres si las operaciones requeridas quedan cubiertas y documentadas. La siguiente tabla compara ese contrato con el código recibido.

| Operación de referencia del curso | Situación en la implementación revisada |
|---|---|
| Registro y login | Implementados como `/api/users/register` y `/api/users/login`. |
| Login con username y contraseña | La implementación actual utiliza email y contraseña. |
| Obtener usuario autenticado (`GET /api/users/me`) | No se encontró ese endpoint. |
| Consultar perfil | Implementado; la respuesta actual no incluye contadores ni `isFollowing`. |
| Publicar tweet | Implementado como `POST /api/posts`. |
| Consultar publicaciones por usuario | Implementado como `GET /api/posts/user/{username}`, sin paginación. |
| Feed propio y de usuarios seguidos | `GET /api/posts` devuelve todas las publicaciones; no aplica ese filtro. |
| Paginación del feed y de publicaciones por usuario | No se encontró implementada. |
| Seguir usuario | Implementado como `POST /api/follows/{username}`. |
| Listas de seguidores y seguidos | No se encontraron endpoints de listas; existen contadores. |
| Respuesta paginada estándar | No se encontró implementada. |
| Seed de al menos 25 usuarios y 60 publicaciones | No se identificó un script de seed en los archivos del backend recibidos. |