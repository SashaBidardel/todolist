# To Do API REST - Proyecto Final (DWES)

## 📝 Descripción del Proyecto
Este proyecto es una solución integral para la gestión de tareas personales, desarrollada bajo el framework **Spring Boot 3.4** y **Java 21**[cite: 1]. Implementa una arquitectura robusta orientada a servicios para cumplir con los requisitos del módulo de **Desarrollo Web en Entorno Servidor**[cite: 1].

## 🛠️ Arquitectura y Organización 
Se ha seguido una arquitectura **MVC / Multicapa** estricta para garantizar la separación de responsabilidades[cite: 1]:
*   **Controller:** Gestión de endpoints y documentación Swagger[cite: 1].
*   **Service:** Lógica de negocio, validaciones de seguridad y control de propiedad[cite: 1].
*   **Repository:** Abstracción de la base de datos mediante Spring Data JPA[cite: 1].
*   **DTOs:** Utilizados para el intercambio de datos eficiente y para el Dashboard, evitando la exposición de entidades sensibles[cite: 1].

## 📊 Modelo de Datos y Persistencia 
El modelo extiende el dominio base propuesto con una personalización avanzada para mejorar la productividad[cite: 1]:

### Entidades y Relaciones
*   **User:** Gestión de perfiles con roles `ADMIN`, `GESTOR` y `USER`[cite: 1].
*   **Task (Ampliación):** Se han añadido atributos para transformar la lista en una herramienta profesional[cite: 1]:
    *   **Prioridad (`priority`):** Permite el filtrado de tareas críticas (LOW, MEDIUM, HIGH)[cite: 1].
    *   **Fecha Límite (`deadline`):** Control de plazos de entrega[cite: 1].
    *   **Indicador de Importancia (`important`):** Atributo booleano para destacar tareas subjetivamente[cite: 1].
    *   **Estimación de Tiempo (`estimatedTime`):** Planificación de la carga de trabajo[cite: 1].
*   **Category:** Relación **1:N** con tareas (incluye reasignación automática a "General" al borrar)[cite: 1].
*   **Tag:** Relación **N:M** con tareas para una organización transversal[cite: 1].

## 🚀 Endpoints y Lógica Funcional 
Se han implementado todos los endpoints obligatorios y las funcionalidades de ampliación[cite: 1]:

### 1. Administrador (Gestión Global)
*   **CRUD Usuarios:** Control total sobre el listado y eliminación de cuentas[cite: 1].
*   **CRUD Categorías:** Gestión administrativa del catálogo de categorías[cite: 1].
*   **Gestión de Rangos:**
    *   `PATCH /api/users/{id}/promote`: **Promocionar** usuario a GESTOR[cite: 1].
    *   `PATCH /api/users/{id}/demote`: **Degradar** gestor a USER[cite: 1].

### 2. Gestor (Infraestructura de Datos)
*   **CRUD Categorías:** Capacidad para organizar las categorías disponibles en el sistema[cite: 1].

### 3. Usuario (Gestión Personal)
*   **CRUD Tarea:** Consultas y filtrado por todos los campos (prioridad, fecha, etc.)[cite: 1].
*   **CRUD Tag:** Creación, asignación y eliminación de etiquetas en sus propias tareas[cite: 1].
*   **Modificar Perfil:** El usuario puede editar sus propios datos (nombre, email, contraseña) mediante `PUT /api/users/{id}`[cite: 1].
*   **Dashboard:** Información estadística sobre tareas por categorías y estados[cite: 1].

## 🔐 Seguridad: Matriz de Permisos 
Configuración basada en **Spring Security 6** con autenticación **HTTP Basic (Stateless)**[cite: 1].

| Rol | Permisos |
| :--- | :--- |
| **USER** | CRUD de sus propias tareas y tags; listar categorías; editar su propio perfil[cite: 1]. |
| **GESTOR** | Todo lo de USER + CRUD de Categorías[cite: 1]. |
| **ADMIN** | CRUD Usuarios, CRUD Categorías y promoción/degradación de usuarios[cite: 1]. |

*   **Cifrado:** Contraseñas protegidas mediante `BCryptPasswordEncoder`[cite: 1].
*   **Validación Propietario:** El sistema verifica en el Service que un usuario solo pueda editar o borrar sus propios recursos, devolviendo `403 Forbidden` en intentos no autorizados[cite: 1].

## 📖 Documentación Swagger/OpenAPI 
La API cuenta con documentación profesional accesible en:
👉 `http://localhost:8080/swagger-ui/index.html`[cite: 1]

*   **@Operation:** Descripciones detalladas para cada método (GET, POST, PUT, DELETE)[cite: 1].
*   **@Parameter:** Documentación de parámetros de entrada y obligatoriedad[cite: 1].
*   **Resolución de Conflictos:** Se ha resuelto la colisión entre la entidad `Tag` y la anotación de Swagger mediante nombres cualificados[cite: 1].

## ⚙️ Tecnologías Utilizadas
*   **Framework:** Spring Boot 3.4
*   **Persistencia:** Spring Data JPA / Hibernate
*   **Base de Datos:** MySQL / MariaDB
*   **Seguridad:** Spring Security (Stateless)
*   **Documentación:** SpringDoc OpenAPI 3