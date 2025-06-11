# Documentación Técnica de la Aplicación TodoListSpringBoot

En este documento se establece los principales cambios y ampliaciones realizados en la aplicación TodoListSpringBoot.

## 1. Nuevas Clases y Métodos Implementados

### Clases Java

- **UsuarioNoAutorizadoException**: Excepción personalizada para denegar el acceso a recursos restringidos a administradores.
- **GlobalControllerAdvice**: Añade automáticamente el usuario autenticado al modelo de todas las vistas.
- **UsuarioController**: Ampliado con métodos para:
  - Listar usuarios (`/registrados`)
  - Mostrar descripción de usuario (`/registrados/{id}`)
  - Bloquear y habilitar usuarios (`/registrados/{id}/bloquear` y `/registrados/{id}/habilitar`)
  - Protección de rutas solo para administradores

### Métodos en UsuarioService

- `public boolean existeAdministrador()`
- `public void bloquearUsuario(Long id)`
- `public void habilitarUsuario(Long id)`
- `public List<UsuarioData> findAllUsuarios()`

### Métodos en UsuarioRepository

- `boolean existsByAdminTrue()`
- `Optional<Usuario> findByEmail(String email)`

## 2. Plantillas Thymeleaf Añadidas o Modificadas

- `listaUsuarios.html`: Listado de usuarios con acciones de bloqueo/habilitación y enlace a descripción.
- `descripcionUsuario.html`: Muestra los datos de un usuario (sin contraseña).
- `fragments.html`: Navbar adaptado para mostrar enlaces según el rol y estado de autenticación.
- `formRegistro.html`: Checkbox para alta como administrador (solo si no existe uno).

## 3. Explicación de Tests Implementados

Se han añadido y ampliado tests en las siguientes capas:

### Repositorio

- **UsuarioRepositoryTest**: Verifica la existencia de un administrador y la recuperación de usuarios.

### Servicio

- **UsuarioServiceTest**: Comprueba la lógica de login, registro, existencia de administrador, bloqueo y habilitación de usuarios, y listado de usuarios.

### Controlador

- **UsuarioWebTest**: Mockea el servicio de usuario para probar los endpoints web, incluyendo:
  - Acceso restringido a `/registrados` y `/registrados/{id}` solo para administradores.
  - Mensajes de error en login para usuarios bloqueados.
  - Acciones de bloqueo/habilitación.
  
## 4. Explicación de Código Fuente Relevante para Bloquear/Habilitar un usuario

### 4.1. Protección de Rutas Solo para Administradores

En UsuarioController se añade un método privado para comprobar si el usuario autenticado es administrador. Caso contrario se lanza la excepción UsuarioNoAutorizadoException:

```java
Long idUsuario = managerUserSession.usuarioLogeado();
UsuarioData usuario = usuarioService.findById(idUsuario);
if (usuario == null || !Boolean.TRUE.equals(usuario.getAdmin())) {
    throw new UsuarioNoAutorizadoException();
```

Este método se invoca al inicio de los métodos que gestionan el listado y la descripción de usuarios.

### 4.2 Bloqueo y Habilitación de Usuarios

**Servicio**: Se encarga de la lógica para bloquear o habilitar a un usuario dependiendo de su identificador.

```java
@Transactional
public void bloquearUsuario(Long id) {
    Usuario usuario = usuarioRepository.findById(id).orElseThrow();
    usuario.setBloqueado(true);
    usuarioRepository.save(usuario);
}

@Transactional
public void habilitarUsuario(Long id) {
    Usuario usuario = usuarioRepository.findById(id).orElseThrow();
    usuario.setBloqueado(false);
    usuarioRepository.save(usuario);
}
```

**Controlador**: Contiene los endpoints para bloquear o habilitar un usuario, los cuales luego de realizar la acción realizan una redirección a `/registrados`.

```java
@PostMapping("/registrados/{id}/bloquear")
public String bloquearUsuario(@PathVariable("id") Long idUsuario) {
    comprobarAdmin();
    usuarioService.bloquearUsuario(idUsuario);
    return "redirect:/registrados";
}

@PostMapping("/registrados/{id}/habilitar")
public String habilitarUsuario(@PathVariable("id") Long idUsuario) {
    comprobarAdmin();
    usuarioService.habilitarUsuario(idUsuario);
    return "redirect:/registrados";
}
```

**Vista (Thymeleaf)**: Esta contiene los botones para la funcionalidad, los cuales aparecen dependiendo si el usuario ya ha sido bloqueado o no.

```html
<tr th:each="usuario : ${usuarios}">
    <td th:text="${usuario.id}"></td>
    <td th:text="${usuario.email}"></td>
    <td>
        <a th:href="@{/registrados/{id}(id=${usuario.id})}">Ver descripción</a>
        <form th:action="@{/registrados/{id}/bloquear(id=${usuario.id})}" method="post"
              th:if="${!usuario.bloqueado}" style="display:inline;">
            <button class="btn btn-warning btn-sm" type="submit">Bloquear</button>
        </form>
        <form th:action="@{/registrados/{id}/habilitar(id=${usuario.id})}" method="post"
              th:if="${usuario.bloqueado}" style="display:inline;">
            <button class="btn btn-success btn-sm" type="submit">Habilitar</button>
        </form>
    </td>
</tr>
```

En esta funcionalidad, el administrador puede bloquear o habilitar usuarios desde el listado. El botón cambia según el estado del usuario. Las acciones POST llaman a los métodos del controlador que a su vez usan el servicio para actualizar el estado en la base de datos.

### 4.3 Gestión de Login de Usuario Bloqueado

En el método `login` de `UsuarioService` se tiene lo siguiente para establecer si el usuario ha sido encontrado, bloqueado o existe un error con la contraseña:

```java
@Transactional(readOnly = true)
public LoginStatus login(String eMail, String password) {
    Optional<Usuario> usuario = usuarioRepository.findByEmail(eMail);
    if (!usuario.isPresent()) {
        return LoginStatus.USER_NOT_FOUND;
    } else if (Boolean.TRUE.equals(usuario.get().getBloqueado())) {
        return LoginStatus.BLOCKED;
    } else if (!usuario.get().getPassword().equals(password)) {
        return LoginStatus.ERROR_PASSWORD;
    } else {
        return LoginStatus.LOGIN_OK;
    }
}
```

En el controlador de login, si el usuario está bloqueado, se muestra un mensaje de error en la vista:

```java
else if (loginStatus == UsuarioService.LoginStatus.BLOCKED) {
    model.addAttribute("error", "El usuario está bloqueado. Contacte con el administrador.");
    return "formLogin";
}
```

## 5. Resumen

- Se ha implementado la Página Acerca de para mostrar información del autor.
- Se ha agregado una barra menú visible en todas las páginas principales con la información del usuario, misma que muestra los enlaces a login y registro cuando no hay un usuario autenticado.
- Se ha implementado una página con el listado de todos los usuarios, donde se observa su identificador y correo electrónico.
- Se ha implementado la gestión de usuarios administradores, permitiendo que solo uno exista y que tenga acceso exclusivo a la administración de usuarios.
- Se ha agregado una página con la descripción de los usuarios a la cual se puede acceder con un enlace en la página de lista de usuarios.
- Se ha añadido la funcionalidad de bloqueo y habilitación de usuarios, con control visual y de backend.
- Se ha protegido el acceso a las páginas de administración y descripción de usuarios.
- Se han añadido mensajes de error claros y personalizados para accesos no autorizados y usuarios bloqueados.
- Se han ampliado los tests para cubrir las nuevas funcionalidades y asegurar la robustez de la aplicación.
