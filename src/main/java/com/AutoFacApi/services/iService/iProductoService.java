package com.AutoFacApi.services.iService;

import com.AutoFacApi.entity.Producto;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface iProductoService {
    List<Producto> findAll();
    Producto guardar(Producto producto);
    Producto actualizar(Long id, Producto producto);
    void eliminar(Long id);

    /** Valida y guarda la imagen; devuelve la ruta relativa a la API que se guarda en imagenUrl. */
    String guardarImagen(MultipartFile imagen) throws IOException;

    /** Devuelve el archivo de una imagen subida, o null si el nombre no es valido o no existe. */
    Resource cargarImagen(String nombre) throws IOException;
}
