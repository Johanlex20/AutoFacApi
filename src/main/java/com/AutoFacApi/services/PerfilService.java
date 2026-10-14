package com.AutoFacApi.services;

import com.AutoFacApi.dto.CambioPasswordRequest;
import com.AutoFacApi.dto.PerfilRequest;
import com.AutoFacApi.dto.PerfilResponse;
import com.AutoFacApi.entity.Usuario;
import com.AutoFacApi.exception.RecursoNoEncontradoException;
import com.AutoFacApi.repository.iUsuarioRepository;

import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PerfilService {

    private static final String SUBCARPETA_FOTOS = "perfiles";
    // Es lo que se guarda en Usuario.fotoUrl
    private static final String PREFIJO_FOTO = "/perfil/foto/";

    private final iUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ImagenService imagenService;

    public PerfilService(iUsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder,
                         JwtService jwtService,
                         ImagenService imagenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.imagenService = imagenService;
    }

    public PerfilResponse obtener(String email) {
        return aRespuesta(buscar(email), null);
    }

    public PerfilResponse actualizar(String emailActual, PerfilRequest datos) {
        Usuario usuario = buscar(emailActual);

        String emailNuevo = datos.getEmail().trim();
        boolean cambioCorreo = !emailNuevo.equals(usuario.getEmail());

        if (cambioCorreo && usuarioRepository.findByEmail(emailNuevo).isPresent()) {
            throw new IllegalStateException("Ese correo ya está en uso por otro usuario.");
        }

        usuario.setEmail(emailNuevo);
        usuario.setNombre(limpiar(datos.getNombre()));
        usuario.setTelefono(limpiar(datos.getTelefono()));
        usuarioRepository.save(usuario);

        // El token lleva el correo dentro: si cambia, el anterior deja de servir, asi que se entrega uno nuevo
        String token = cambioCorreo ? jwtService.generarToken(usuario.getEmail(), usuario.getRol()) : null;
        return aRespuesta(usuario, token);
    }

    public void cambiarPassword(String email, CambioPasswordRequest datos) {
        Usuario usuario = buscar(email);

        if (!passwordEncoder.matches(datos.getPasswordActual(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (passwordEncoder.matches(datos.getPasswordNueva(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta a la actual.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(datos.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }

    public PerfilResponse subirFoto(String email, MultipartFile foto) throws IOException {
        Usuario usuario = buscar(email);

        String anterior = usuario.getFotoUrl();
        usuario.setFotoUrl(PREFIJO_FOTO + imagenService.guardar(SUBCARPETA_FOTOS, foto));
        usuarioRepository.save(usuario);

        borrarArchivo(anterior);
        return aRespuesta(usuario, null);
    }

    public PerfilResponse quitarFoto(String email) {
        Usuario usuario = buscar(email);

        String anterior = usuario.getFotoUrl();
        usuario.setFotoUrl(null);
        usuarioRepository.save(usuario);

        borrarArchivo(anterior);
        return aRespuesta(usuario, null);
    }

    public Resource cargarFoto(String nombre) throws IOException {
        return imagenService.cargar(SUBCARPETA_FOTOS, nombre);
    }

    private Usuario buscar(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }

    private void borrarArchivo(String fotoUrl) {
        if (fotoUrl != null && fotoUrl.startsWith(PREFIJO_FOTO)) {
            imagenService.borrar(SUBCARPETA_FOTOS, fotoUrl.substring(PREFIJO_FOTO.length()));
        }
    }

    private String limpiar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private PerfilResponse aRespuesta(Usuario u, String token) {
        return new PerfilResponse(u.getEmail(), u.getRol(), u.getNombre(), u.getTelefono(), u.getFotoUrl(), token);
    }
}
