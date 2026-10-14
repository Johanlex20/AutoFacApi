package com.AutoFacApi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambioPasswordRequest {

    @NotBlank(message = "Escribe tu contraseña actual.")
    private String passwordActual;

    @NotBlank(message = "Escribe la nueva contraseña.")
    @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres.")
    private String passwordNueva;
}
