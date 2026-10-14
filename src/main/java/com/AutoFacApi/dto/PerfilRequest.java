package com.AutoFacApi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilRequest {

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "El correo no tiene un formato válido.")
    @Size(max = 150, message = "El correo es demasiado largo.")
    private String email;

    @Size(max = 100, message = "El nombre es demasiado largo.")
    private String nombre;

    @Pattern(regexp = "^$|^[+0-9 ()-]{7,20}$", message = "El teléfono solo puede tener números, espacios, +, - y paréntesis (7 a 20 caracteres).")
    private String telefono;
}
