package madstodolist.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping("/cuenta")
    public String cuentaUsuario(Model model, HttpSession session) {
        Long idUsuario = (Long) session.getAttribute("idUsuarioLogeado");
        if (idUsuario == null) {
            return "redirect:/login";
        }
        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);
        return "cuentaUsuario";
    }

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model, HttpSession session) {
        Long idUsuario = (Long) session.getAttribute("idUsuarioLogeado");
        UsuarioData usuario = usuarioService.findById(idUsuario);

        if (usuario == null || !Boolean.TRUE.equals(usuario.getAdmin())) {
            throw new UsuarioNoAutorizadoException();
        }

        List<UsuarioData> usuarios = usuarioService.findAllUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable("id") Long idUsuario, Model model) {
        UsuarioData usuario = usuarioService.findById(idUsuario);

        if (usuario == null || !Boolean.TRUE.equals(usuario.getAdmin())) {
            throw new UsuarioNoAutorizadoException();
        }

        model.addAttribute("usuarioDescripcion", usuario);
        return "descripcionUsuario";
    }

    @PostMapping("/registrados/{id}/bloquear")
    public String bloquearUsuario(@PathVariable("id") Long idUsuario) {
        Long idAdmin = managerUserSession.usuarioLogeado();
        UsuarioData usuarioAdmin = usuarioService.findById(idAdmin);

        if (usuarioAdmin == null || !Boolean.TRUE.equals(usuarioAdmin.getAdmin())) {
            throw new UsuarioNoAutorizadoException();
        }

        usuarioService.bloquearUsuario(idUsuario);
        return "redirect:/registrados";
    }

    @PostMapping("/registrados/{id}/habilitar")
    public String habilitarUsuario(@PathVariable("id") Long idUsuario) {
        Long idAdmin = managerUserSession.usuarioLogeado();
        UsuarioData usuarioAdmin = usuarioService.findById(idAdmin);

        if (usuarioAdmin == null || !Boolean.TRUE.equals(usuarioAdmin.getAdmin())) {
            throw new UsuarioNoAutorizadoException();
        }

        usuarioService.habilitarUsuario(idUsuario);
        return "redirect:/registrados";
    }
}
