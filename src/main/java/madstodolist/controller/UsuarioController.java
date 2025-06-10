package madstodolist.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

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
    public String listadoUsuarios(Model model) {
        List<UsuarioData> usuarios = usuarioService.findAllUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable("id") Long idUsuario, Model model) {
        UsuarioData usuario = usuarioService.findById(idUsuario);
        if (usuario == null) {
            // Puedes redirigir o mostrar una página de error si el usuario no existe
            return "redirect:/registrados";
        }
        model.addAttribute("usuarioDescripcion", usuario);
        return "descripcionUsuario";
    }
}
