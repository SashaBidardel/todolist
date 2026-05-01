# To Do API REST - Proyecto Final (DWES)

## 📝 Descripción del Proyecto
Este proyecto consiste en el desarrollo de una **API REST** robusta para la gestión de tareas (To-Do List) utilizando **Java 21** y **Spring Boot 3.4**. La aplicación permite una organización avanzada mediante categorías y etiquetas, gestiona usuarios con seguridad basada en roles y aplica reglas de negocio para garantizar la integridad de los datos.

Proyecto desarrollado para el módulo de **Desarrollo Web en Entorno Servidor (DWES)**.

## 🚀 Documentación Interactiva (Swagger/OpenAPI)
La API cuenta con una capa de documentación profesional accesible en tiempo real:
*   **URL de Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
*   **Funcionalidades:** 
    *   Pruebas de endpoints sin necesidad de herramientas externas.
    *   Especificación de códigos de estado HTTP (200, 201, 204, 401, 403, 404).
    *   Esquemas de datos detallados para cada entidad.

## 📊 Modelo de Datos y Base de Datos
La persistencia se gestiona con **Spring Data JPA** sobre una base de datos relacional (**MySQL/MariaDB**)

### Entidades Principales
*   **User:** Almacena credenciales (cifradas con BCrypt), perfil y rol (`ADMIN`, `GESTOR`, `USER`).
*   **Task:** Entidad central que incluye `priority` (LOW, MEDIUM, HIGH), `deadline` (fecha límite) y `status`.
*   **Category:** Agrupación temática de tareas.
*   **Tag:** Etiquetas descriptivas en relación **Many-to-Many** con las tareas.

### Detalles de la Base de Datos
*   **Relación Circular Seguro:** Se ha configurado el borrado lógico/reasignación para que, al eliminar una categoría, las tareas se muevan automáticamente a la categoría "General".
*   **Cifrado de Datos:** Uso de `passwordEncoder` para asegurar que las contraseñas y otros datos sensibles (como siglas de docentes) nunca se almacenen en texto plano.


## 🛠️ Lógica de Negocio y Ampliaciones (Actividad 1)

### 1. Gestión de Prioridad y Deadline
Se han incorporado atributos para transformar la lista en una herramienta de productividad real:
*   **Prioridad:** Permite el filtrado de tareas críticas.
*   **Deadline:** Control de plazos para evitar tareas fuera de fecha.

### 2. Resolución de Conflictos Técnicos
Se ha resuelto con éxito la colisión de nombres entre la entidad del modelo `Tag` y la anotación de Swagger mediante el uso de nombres completamente cualificados (`io.swagger.v3.oas.annotations.tags.Tag`), garantizando un código limpio y una documentación funcional.

### 3. Control de Propiedad
El sistema valida en cada operación de edición o borrado que el usuario autenticado sea el **propietario** de la tarea .

### 4. Indicador de Importancia (`important`):** Atributo booleano que permite al usuario destacar tareas de forma subjetiva, independientemente de su prioridad técnica.

### 5. Estimación de Tiempo (`estimatedTime`):** Campo de texto flexible (ej: "2 horas") que permite una planificación realista de la carga de trabajo.


## 🔐 Seguridad: Matriz de Permisos
Configuración basada en **Spring Security 6** con autenticación **HTTP Basic**.

| Rol | Permisos |
| :--- | :--- |
| **USER** | Crear, ver, editar y borrar sus propias tareas y tags y listar categorias . |
| **GESTOR** | Además de lo anterior, CRUD de Categorías . |
| **ADMIN** | CRUD usuarios y categorias y promoción y degradación de usuarios. |

## 🛠️ Gestión Global de Excepciones
El proyecto utiliza `@RestControllerAdvice` para estandarizar las respuestas de error en formato JSON:
*   **201 Created:** Para registros y nuevas tareas.
*   **204 No Content:** Para eliminaciones exitosas.
*   **403 Forbidden:** Cuando un usuario intenta acceder a datos de otro.
*   **404 Not Found:** Cuando el ID solicitado no existe.

## ⚙️ Tecnologías Utilizadas
*   **Lenguaje:** Java 21
*   **Framework:** Spring Boot 3.4
*   **Persistencia:** Spring Data JPA / Hibernate
*   **Seguridad:** Spring Security (BCrypt)
*   **Documentación:** SpringDoc OpenAPI 3
*   **Base de Datos:** MySQL / MariaDB

## 🚀 Instalación y Configuración
1.  Clonar el repositorio.
2.  Configurar las credenciales en `src/main/resources/application.properties`.
3.  Ejecutar la aplicación con el comando: `./mvnw spring-boot:run`.
4.  Acceder a Swagger para verificar el despliegue.

---