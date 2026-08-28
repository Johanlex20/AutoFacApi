package com.AutoFacApi.controller;

import com.AutoFacApi.dto.LoginRequest;
import com.AutoFacApi.dto.LoginResponse;
import com.AutoFacApi.entity.Usuario;
import com.AutoFacApi.repository.iUsuarioRepository;
import com.AutoFacApi.services.JwtService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final iUsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                           iUsuarioRepository usuarioRepository,
                           JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol());

        return ResponseEntity.ok(new LoginResponse(token, usuario.getEmail(), usuario.getRol()));
    }
}
