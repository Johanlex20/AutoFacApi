package com.AutoFacApi.controller;

import com.AutoFacApi.dto.CambioPasswordRequest;
import com.AutoFacApi.dto.PerfilRequest;
import com.AutoFacApi.dto.PerfilResponse;
import com.AutoFacApi.services.ImagenService;
import com.AutoFacApi.services.PerfilService;

import jakarta.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Perfil del usuario con sesion iniciada: siempre opera sobre quien trae el token, nunca sobre otro id. */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public PerfilResponse obtener(Authentication auth) {
        return perfilService.obtener(auth.getName());
    }

    @PutMapping
    public PerfilResponse actualizar(Authentication auth, @Valid @RequestBody PerfilRequest datos) {
        return perfilService.actualizar(auth.getName(), datos);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(Authentication auth, @Valid @RequestBody CambioPasswordRequest datos) {
        perfilService.cambiarPassword(auth.getName(), datos);
    }

    @PostMapping("/foto")
    public PerfilResponse subirFoto(Authentication auth, @RequestParam("foto") MultipartFile foto) throws IOException {
        return perfilService.subirFoto(auth.getName(), foto);
    }

    @DeleteMapping("/foto")
    public PerfilResponse quitarFoto(Authentication auth) {
        return perfilService.quitarFoto(auth.getName());
    }

    // Publico (ver SecurityConfig): un <img> no puede mandar el token. El nombre es un UUID imposible de adivinar.
    @GetMapping("/foto/{nombre}")
    public ResponseEntity<Resource> verFoto(@PathVariable String nombre) throws IOException {
        Resource recurso = perfilService.cargarFoto(nombre);
        if (recurso == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(ImagenService.tipoPorNombre(nombre))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(recurso);
    }
}
