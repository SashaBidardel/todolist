# To Do API REST - Proyecto Final (DWES)

## 📝 Descripción del Proyecto
Este proyecto es una solución integral para la gestión de tareas personales, desarrollada bajo el framework **Spring Boot 3.4** y **Java 21**. Implementa una arquitectura robusta orientada a servicios para cumplir con los requisitos del módulo de **Desarrollo Web en Entorno Servidor**.

## 🛠️ Arquitectura y Organización 
Se ha seguido una arquitectura **MVC / Multicapa** estricta para garantizar la separación de responsabilidades:
*   **Controller:** Gestión de endpoints y documentación Swagger.
*   **Service:** Lógica de negocio, validaciones de seguridad y control de propiedad.
*   **Repository:** Abstracción de la base de datos mediante Spring Data JPA.
*   **DTOs:** Utilizados para el intercambio de datos eficiente y para el Dashboard, evitando la exposición de entidades sensibles.

## 📊 Modelo de Datos y Persistencia 
El modelo extiende el dominio base propuesto con una personalización avanzada para mejorar la productividad:

### Entidades y Relaciones
*   **User:** Gestión de perfiles con roles `ADMIN`, `GESTOR` y `USER`.
*   **Task (Ampliación):** Se han añadido atributos para transformar la lista en una herramienta profesional:
    *   **Prioridad (`priority`):** Permite el filtrado de tareas críticas (LOW, MEDIUM, HIGH).
    *   **Fecha Límite (`deadline`):** Control de plazos de entrega.
    *   **Indicador de Importancia (`important`):** Atributo booleano para destacar tareas subjetivamente.
    *   **Estimación de Tiempo (`estimatedTime`):** Planificación de la carga de trabajo.
*   **Category:** Relación **1:N** con tareas (incluye reasignación automática a "General" al borrar).
*   **Tag:** Relación **N:M** con tareas para una organización transversal.

## 🚀 Endpoints y Lógica Funcional 
Se han implementado todos los endpoints obligatorios y las funcionalidades de ampliación:

### 1. Administrador (Gestión Global)
*   **CRUD Usuarios:** Control total sobre el listado y eliminación de cuentas.
*   **CRUD Categorías:** Gestión administrativa del catálogo de categorías.
*   **Gestión de Rangos:**
    *   `PATCH /api/users/{id}/promote`: **Promocionar** usuario a GESTOR.
    *   `PATCH /api/users/{id}/demote`: **Degradar** gestor a USER.

### 2. Gestor (Infraestructura de Datos)
*   **CRUD Categorías:** Capacidad para organizar las categorías disponibles en el sistema.

### 3. Usuario (Gestión Personal)
*   **CRUD Tarea:** Consultas y filtrado por todos los campos (prioridad, fecha, etc.).
*   **CRUD Tag:** Creación, asignación y eliminación de etiquetas en sus propias tareas.
*   **Modificar Perfil:** El usuario puede editar sus propios datos (nombre, email, contraseña) mediante `PUT /api/users/{id}`.
*   **Dashboard:** Información estadística sobre tareas por categorías y estados.
*   Eliminar **Tags** a tareas creadas. 
*   Asignar **Tags** a tareas creadas.
*   Buscar tareas con **tags** seleccionados.

## 🔐 Seguridad: Matriz de Permisos 
Configuración basada en **Spring Security 6** con autenticación **HTTP Basic (Stateless)**.

| Rol | Permisos |
| :--- | :--- |
| **USER** | CRUD de sus propias tareas y tags; listar categorías; editar su propio perfil. |
| **GESTOR** | Todo lo de USER + CRUD de Categorías. |
| **ADMIN** | CRUD Usuarios, CRUD Categorías y promoción/degradación de usuarios. |

*   **Cifrado:** Contraseñas protegidas mediante `BCryptPasswordEncoder`.
*   **Validación Propietario:** El sistema verifica en el Service que un usuario solo pueda editar o borrar sus propios recursos, devolviendo `403 Forbidden` en intentos no autorizados.

## 📖 Documentación Swagger/OpenAPI 
La API cuenta con documentación profesional accesible en:
👉 `http://localhost:8080/swagger-ui/index.html`

*   **@Operation:** Descripciones detalladas para cada método (GET, POST, PUT, DELETE).
*   **@Parameter:** Documentación de parámetros de entrada y obligatoriedad.
*   **Resolución de Conflictos:** Se ha resuelto la colisión entre la entidad `Tag` y la anotación de Swagger mediante nombres cualificados.

## User Admin
 username: admin
 password: 1234

## ⚙️ Tecnologías Utilizadas
*   **Framework:** Spring Boot 3.4
*   **Persistencia:** Spring Data JPA / Hibernate
*   **Base de Datos:** MySQL / MariaDB
*   **Seguridad:** Spring Security (Stateless)
*   **Documentación:** SpringDoc OpenAPI 3