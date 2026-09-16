package com.AutoFacApi.controller;

import com.AutoFacApi.entity.Documento;
import com.AutoFacApi.services.iService.iDocumentoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping(value = "/api/documento")
public class DocumentoController {
  
   @Autowired
    private iDocumentoService documentoService;

    @Value("${documentos.upload-dir}")
    private String uploadDir;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Documento> findAll() {
        return documentoService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Documento subir(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        return documentoService.guardar(archivo);
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long id) throws MalformedURLException {
        Documento documento = documentoService.findById(id);
        if (documento == null) {
            return ResponseEntity.notFound().build();
        }

        Path archivo = Paths.get(uploadDir).resolve(documento.getNombreArchivo());
        Resource recurso = new UrlResource(archivo.toUri());

        MediaType tipo;
        try {
            tipo = MediaType.parseMediaType(documento.getContentType());
        } catch (Exception e) {
            tipo = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documento.getNombreOriginal() + "\"")
                .body(recurso);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) throws IOException {
        documentoService.eliminar(id);
    }

    @PutMapping("/{id}")
    public Documento actualizar(
            @PathVariable Long id,
            @RequestParam(required = false) String nombreOriginal,
            @RequestParam(required = false) MultipartFile archivo) throws IOException {
        return documentoService.actualizar(id, nombreOriginal, archivo);
    }


}
