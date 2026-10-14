package com.AutoFacApi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerfilResponse {
    private String email;
    private String rol;
    private String nombre;
    private String telefono;
    private String fotoUrl;
    /** Solo viene cuando cambio el correo: el token anterior queda ligado al correo viejo. */
    private String token;
}
