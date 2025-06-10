package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
public class HomeWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private madstodolist.authentication.ManagerUserSession managerUserSession;

    @Test
    public void aboutMuestraLoginYRegistroSiNoHayUsuario() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Login")))
                .andExpect(content().string(containsString("Registro")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Cerrar sesión"))));
    }

    @Test
    public void aboutMuestraBarraUsuarioSiHayUsuario() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);
        usuario.setNombre("Fernando");

        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cerrar sesión")))
                .andExpect(content().string(containsString("Fernando")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Login"))));
    }
}
