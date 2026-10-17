package com.AutoFacApi.dto;

public class LoginResponse {

    private final String token;
    private final String email;
    private final String rol;

    public LoginResponse(String token, String email, String rol) {
        this.token = token;
        this.email = email;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }
}
