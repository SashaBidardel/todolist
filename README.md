# To Do API REST - Proyecto Final

## 📝 Descripción del Proyecto
Este proyecto consiste en el desarrollo de una **API REST** para la gestión de tareas (To-Do List) utilizando **Java** y **Spring Boot**. La aplicación permite organizar tareas mediante categorías y etiquetas, gestionar usuarios con diferentes niveles de acceso y aplicar reglas de negocio personalizadas.

Proyecto desarrollado para el módulo de **Desarrollo Web en Entorno Servidor (DWES)**.

## 📝 Justificación de la Ampliación del Modelo (Actividad 1)

Para cumplir con los requisitos de la Actividad 1, se ha rediseñado el modelo de datos original incorporando atributos que aportan valor real a la gestión de tareas. A continuación se detallan las decisiones adoptadas:

### 1. Nivel de Prioridad de la Tarea (`Priority`)
Se ha implementado mediante un tipo enumerado (`LOW`, `MEDIUM`, `HIGH`).
* **Justificación:** En cualquier sistema de gestión de tareas, no todos los elementos tienen la misma urgencia. Permitir al usuario clasificar sus tareas por prioridad facilita la toma de decisiones sobre qué abordar primero, cumpliendo con el objetivo de una "gestión más eficiente".
* **Impacto Técnico:** Mejora la capacidad de filtrado y ordenación en las consultas a la base de datos (Ej: "Ver tareas de alta prioridad primero").

### 2. Fecha Límite de Finalización (`Deadline`)
Se ha incorporado un campo de tipo `LocalDateTime`.
* **Justificación:** Una tarea sin fecha de entrega suele quedar en el olvido. La inclusión de un `deadline` permite al sistema (y al usuario) controlar el cumplimiento de plazos. Es un atributo esencial para transformar una simple lista de notas en una herramienta de productividad real.
* **Impacto Técnico:** Permite implementar lógica de negocio avanzada en el futuro, como notificaciones de tareas próximas a vencer o filtros de tareas "fuera de plazo".

### 3. Fecha de Creación Automatizada (`createdAt`)
Aunque el diagrama base la mencionaba, se ha reforzado su implementación mediante la anotación `@PrePersist`.
* **Justificación:** Proporciona un registro histórico exacto de cuándo se detectó la necesidad de la tarea, permitiendo realizar auditorías de rendimiento y trazabilidad.

## 📂 Gestión de Categorías y Etiquetas
Se ha optado por un modelo de gestión centralizado para garantizar la integridad de la información:
* **Categorías:** Funcionan como un catálogo maestro gestionado exclusivamente por el rol `ADMIN`. Esto evita la redundancia de datos y mantiene una estructura organizativa limpia.
* **Tags (Etiquetas):** Permiten una clasificación transversal y flexible. Los usuarios pueden asignar múltiples etiquetas a una misma tarea, facilitando búsquedas personalizadas y una organización multidimensional.

# Implementación de Categoría por Defecto (General)

Este documento detalla la lógica implementada para asegurar que el sistema siempre cuente con una categoría base ("General") y que ninguna tarea se quede sin categoría asignada.

### 1. Inicialización de Datos (`DataInitializer.java`)

Se asegura la creación del usuario administrador y de la categoría "General" de forma independiente cada vez que arranca la aplicación.

### 2. Asignación Automática al Crear Tareas (`TaskService.java`)
Cuando se crea una tarea, si el usuario no especifica una categoría, el sistema busca y asigna automáticamente la categoría "General".

### 3. Borrado Seguro de Categorías(`CategoryService.java`)
Para evitar que las tareas queden "huérfanas" o que la base de datos lance errores de clave foránea (Foreign Key), al borrar una categoría se mueven todas sus tareas a "General".

### Resumen de Flujo de Datos
- **Persistencia:** La categoría "General" actúa como un ancla en la base de datos.

- **Integridad:** Se utiliza @Transactional para asegurar que el movimiento de tareas y el borrado de la categoría ocurran como una única operación atómica.

## 🔐 Seguridad y Usuarios
Configuración basada en **Spring Security 6** con autenticación **HTTP Basic**.

### 1. Registro de Usuarios (`UserService.java`)
- **Cifrado:** Las contraseñas se encriptan con `BCryptPasswordEncoder` antes de guardarse.
- **Validación:** Se comprueba la existencia previa del `username`.
- **Roles:** A todo nuevo registro se le asigna por defecto el rol `USER`.

### 2. Configuración de Acceso (`SecurityConfig.java`)
- `/api/users/register`: Acceso público (`permitAll`).
- `/api/categories/**`: Solo `ADMIN` puede crear/borrar (`hasAuthority`).
- `/api/tasks/**`: Requiere estar autenticado (`authenticated`).

---


## 🧪 Guía de Pruebas en Postman

| Operación | Método | Endpoint | Credenciales |
| :--- | :---: | :--- | :--- |
| **Registrar** | `POST` | `/api/users/register` | Ninguna (Public) |
| **Ver Categorías** | `GET` | `/api/categories` | Admin o User |
| **Crear Tarea** | `POST` | `/api/tasks` | Cualquier User |
| **Borrar Categoría** | `DELETE` | `/api/categories/{id}` | Solo Admin |


## 📈 Coherencia del Diseño
La elección de estos atributos no es aleatoria:
1.  **Compatibilidad:** Ambos atributos (`priority` y `deadline`) se integran perfectamente con los filtros ya existentes (categorías y etiquetas).
2.  **Escalabilidad:** El modelo está preparado para que, en una fase posterior, se puedan añadir "Recordatorios" basados en el `deadline` o "Paneles de control" basados en la `priority`.
3.  **Simplicidad:** Mantienen el modelo ligero sin sobrecargar la base de datos con información redundante, asegurando una API REST rápida y eficiente.

## 🚀 Funcionalidades Principales
* **Gestión de Usuarios:** Registro y gestión de perfiles.
* **Gestión de Tareas:** Crear, organizar y consultar tareas.
* **Organización:** Clasificación mediante categorías y etiquetas (Tags).
* **Filtros:** Consulta avanzada de información[cite: 80].
* **Seguridad:** Control de acceso basado en roles (ADMIN y USER).

## 🛠️ Tecnologías Utilizadas
* **Lenguaje:** Java
* **Framework:** Spring Boot
* **Persistencia:** Spring Data JPA
* **Seguridad:** Spring Security
* **Base de Datos:** MySQL / MariaDB 
* **Productividad:** Lombok, Spring Boot DevTools
* **Validación:** Jakarta Validation API

## 📊 Modelo de Datos (Ampliaciones Actividad 1)
Se ha implementado el modelo de dominio base [cite: 84, 85] incluyendo las siguientes mejoras requeridas en la **Actividad 1**
1.  **Nivel de Prioridad:** Implementación de un atributo para definir la importancia de la tarea (`LOW`, `MEDIUM`, `HIGH`).
2.  **Fecha Límite (Deadline):** Inclusión de una fecha límite de finalización para mejorar la eficiencia.

### Entidades Principales:
* **User:** Gestiona `username`, `password`, `email`, `fullname` y `role`.
* **Task:** Entidad central con relaciones hacia `User`, `Category` y `Tag`.
* **Category:** Organización de tareas por temática.
* **Tag:** Etiquetas descriptivas (Relación Many-to-Many).

## ⚙️ Configuración e Instalación

### Configuración de Base de Datos
1. Crear una base de datos en MySQL/MariaDB.
2. Ajustar las credenciales en `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mariadb://localhost:3306/nombre_tu_bd
   spring.datasource.username=usuario
   spring.datasource.password=contraseña
   
 ## 🔒 Roles y Seguridad: Matriz de Permisos

La aplicación utiliza **Spring Security** para segmentar el acceso mediante los roles `USER` y `ADMIN`. A continuación se detallan las capacidades técnicas de cada perfil:

### 👤 Rol: USER (Usuario Estándar)
Este rol está diseñado para el usuario final que gestiona su propia productividad. Sus permisos están restringidos por **propiedad de datos**.

* **Gestión de Tareas (Propias):**
    * `CREATE`: Crear nuevas tareas asociándolas automáticamente a su perfil.
    * `READ`: Consultar exclusivamente su propia lista de tareas.
    * `UPDATE`: Modificar detalles de sus tareas (título, prioridad, deadline, estado).
    * `DELETE`: Eliminar sus propias tareas del sistema.
* **Categorías y Etiquetas:**
    * `READ`: Consultar el catálogo de categorías disponibles para clasificar sus tareas.
    * `ASSIGN`: Vincular sus tareas con múltiples etiquetas (Tags) y una categoría específica.
* **Perfil Personal:**
    * `READ/UPDATE`: Gestionar sus propios datos de perfil (`fullname`, `email`, `password`).

### 🔑 Rol: ADMIN (Administrador del Sistema)
Este rol posee privilegios elevados para supervisar el funcionamiento global y realizar tareas de mantenimiento.

* **Gestión de Usuarios (Control Total):**
    * `LIST`: Listar todos los usuarios registrados en la plataforma.
    * `SEARCH`: Buscar usuarios específicos por `username` o `email`.
    * `UPDATE`: Modificar roles (ascender a USER a ADMIN) o desactivar cuentas.
    * `DELETE`: Eliminar usuarios de la base de datos (con borrado en cascada de sus tareas).
* **Mantenimiento Global:**
    * `AUDIT`: Capacidad de ver todas las tareas del sistema para fines de moderación, independientemente del autor.
    * `CATEGORIES`: CRUD completo de categorías globales (Crear, editar o eliminar las categorías que luego usan los usuarios).


---

### 🛠️ Implementación Técnica 
Para garantizar este control uso de anotaciones en la capa de `Controller`:

1.  **Protección por Rol:**
    * `@PreAuthorize("hasRole('ADMIN')")` para endpoints de gestión de usuarios.
    * `@PreAuthorize("hasAnyRole('USER', 'ADMIN')")` para gestión de tareas.
2.  **Protección por Propiedad:**
    * En los métodos `updateTask` y `deleteTask`, validar que el `author.id` coincida con el `Principal` (usuario autenticado).