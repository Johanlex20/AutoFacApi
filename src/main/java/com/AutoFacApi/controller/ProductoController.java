package com.AutoFacApi.controller;

import com.AutoFacApi.entity.Producto;
import com.AutoFacApi.services.ImagenService;
import com.AutoFacApi.services.iService.iProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/api/producto")
public class ProductoController {

    @Autowired
    private iProductoService productoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Producto> findAll() {
        return productoService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto guardar(@RequestBody Producto producto) {
        return productoService.guardar(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }

    @PostMapping("/imagen")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> subirImagen(@RequestParam("imagen") MultipartFile imagen) throws IOException {
        return Map.of("imagenUrl", productoService.guardarImagen(imagen));
    }

    @GetMapping("/imagen/{nombre}")
    public ResponseEntity<Resource> verImagen(@PathVariable String nombre) throws IOException {
        Resource recurso = productoService.cargarImagen(nombre);
        if (recurso == null) {
            return ResponseEntity.notFound().build();
        }

        // El nombre es un UUID: una imagen nueva siempre tiene nombre nuevo, asi que se puede cachear fuerte
        return ResponseEntity.ok()
                .contentType(ImagenService.tipoPorNombre(nombre))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(recurso);
    }
}
