package com.AutoFacApi.services.iService;

import com.AutoFacApi.entity.Producto;

import java.util.List;

public interface iProductoService {
    List<Producto> findAll();
    Producto guardar(Producto producto);
    Producto actualizar(Long id, Producto producto);
    void eliminar(Long id);
}
