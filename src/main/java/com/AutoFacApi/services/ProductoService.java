package com.AutoFacApi.services;

import com.AutoFacApi.entity.Producto;
import com.AutoFacApi.exception.RecursoNoEncontradoException;
import com.AutoFacApi.repository.iProductoRepository;
import com.AutoFacApi.services.iService.iProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService implements iProductoService {

    @Autowired
    private iProductoRepository productoRepository;

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

        existente.setNombre(producto.getNombre());
        existente.setCategoria(producto.getCategoria());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPresentacion(producto.getPresentacion());
        existente.setPrecio(producto.getPrecio());
        existente.setImagenUrl(producto.getImagenUrl());

        return productoRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }
}
