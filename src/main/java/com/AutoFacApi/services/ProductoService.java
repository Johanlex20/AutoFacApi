package com.AutoFacApi.services;

import com.AutoFacApi.entity.Producto;
import com.AutoFacApi.exception.RecursoNoEncontradoException;
import com.AutoFacApi.repository.iProductoRepository;
import com.AutoFacApi.services.iService.iProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductoService implements iProductoService {

    private static final String SUBCARPETA_IMAGENES = "productos";
    // Es lo que se guarda en Producto.imagenUrl cuando la imagen se subio desde el dashboard
    private static final String PREFIJO_IMAGEN = "/producto/imagen/";

    @Autowired
    private iProductoRepository productoRepository;

    @Autowired
    private ImagenService imagenService;

    @Override
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    public Producto guardar(Producto producto) {
        producto.setId(null);
        return productoRepository.save(producto);
    }

    @Override
    public Producto actualizar(Long id, Producto producto) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));

        String imagenAnterior = existente.getImagenUrl();

        existente.setNombre(producto.getNombre());
        existente.setCategoria(producto.getCategoria());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPresentacion(producto.getPresentacion());
        existente.setPrecio(producto.getPrecio());
        existente.setImagenUrl(producto.getImagenUrl());

        Producto guardado = productoRepository.save(existente);

        if (imagenAnterior != null && !imagenAnterior.equals(guardado.getImagenUrl())) {
            borrarArchivoImagen(imagenAnterior);
        }
        return guardado;
    }

    @Override
    public void eliminar(Long id) {
        productoRepository.findById(id).ifPresent(p -> borrarArchivoImagen(p.getImagenUrl()));
        productoRepository.deleteById(id);
    }

    @Override
    public String guardarImagen(MultipartFile imagen) throws IOException {
        return PREFIJO_IMAGEN + imagenService.guardar(SUBCARPETA_IMAGENES, imagen);
    }

    @Override
    public Resource cargarImagen(String nombre) throws IOException {
        return imagenService.cargar(SUBCARPETA_IMAGENES, nombre);
    }

    private void borrarArchivoImagen(String imagenUrl) {
        if (imagenUrl == null || !imagenUrl.startsWith(PREFIJO_IMAGEN)) {
            return; // imagen del frontend (assets/...) u otra ruta: no es nuestra
        }
        imagenService.borrar(SUBCARPETA_IMAGENES, imagenUrl.substring(PREFIJO_IMAGEN.length()));
    }
}
