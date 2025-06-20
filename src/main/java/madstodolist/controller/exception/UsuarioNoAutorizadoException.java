package madstodolist.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "No tiene permisos de administrador")
public class UsuarioNoAutorizadoException extends RuntimeException {
}