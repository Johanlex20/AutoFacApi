package com.AutoFacApi.configuration;

import com.AutoFacApi.entity.Usuario;
import com.AutoFacApi.repository.iUsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final iUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.seed.email:}")
    private String adminEmail;

    @Value("${admin.seed.password:}")
    private String adminPassword;

    public AdminSeeder(iUsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRol("ADMIN");
        usuarioRepository.save(admin);
    }
}
