package com.AutoFacApi.configuration;

import com.AutoFacApi.entity.Usuario;
import com.AutoFacApi.repository.iUsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea (si no existe) el usuario de demostracion: rol DEMO, solo lectura.
 * Sus credenciales son publicas a proposito, por eso SecurityConfig le bloquea
 * cualquier POST/PUT/PATCH/DELETE y DataController le oculta los datos de clientes.
 */
@Component
public class DemoSeeder implements CommandLineRunner {

    private final iUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${demo.seed.email:}")
    private String demoEmail;

    @Value("${demo.seed.password:}")
    private String demoPassword;

    public DemoSeeder(iUsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (demoEmail.isBlank() || demoPassword.isBlank()) {
            return;
        }
        if (usuarioRepository.findByEmail(demoEmail).isPresent()) {
            return;
        }

        Usuario demo = new Usuario();
        demo.setEmail(demoEmail);
        demo.setPasswordHash(passwordEncoder.encode(demoPassword));
        demo.setRol("DEMO");
        demo.setNombre("Usuario demo");
        usuarioRepository.save(demo);
    }
}
