package com.AutoFacApi.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.AutoFacApi.entity.Documento;
import com.AutoFacApi.repository.iDocumentoRepository;
import com.AutoFacApi.services.iService.iDocumentoService;


@Service
public class DocumentoService implements iDocumentoService{
  
  @Autowired
    private iDocumentoRepository idocumentoRepository;

    @Value("${documentos.upload-dir}")
    private String uploadDir;

    @Override
    public List<Documento> findAll() {
        return idocumentoRepository.findAll();
    }

    @Override
    public Documento findById(Long id) {
        return idocumentoRepository.findById(id).orElse(null);
    }

    @Override
    public Documento guardar(MultipartFile archivo) throws IOException {
        Path carpeta = Paths.get(uploadDir);
        Files.createDirectories(carpeta);

        String nombreOriginal = archivo.getOriginalFilename();
        String extension = "";
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }
        String nombreArchivo = UUID.randomUUID() + extension;

        Files.copy(archivo.getInputStream(), carpeta.resolve(nombreArchivo));

        Documento documento = new Documento();
        documento.setNombreOriginal(nombreOriginal);
        documento.setNombreArchivo(nombreArchivo);
        documento.setTamanoBytes(archivo.getSize());
        documento.setContentType(archivo.getContentType());
        documento.setFechaSubida(LocalDateTime.now());

        return idocumentoRepository.save(documento);
    }

    @Override
    public void eliminar(Long id) throws IOException {
      Documento documento = idocumentoRepository.findById(id).orElse(null);
      if (documento == null) {
          return;
      }

      Path archivo = Paths.get(uploadDir).resolve(documento.getNombreArchivo());
      Files.deleteIfExists(archivo);

      idocumentoRepository.deleteById(id);
    }

    @Override
    public Documento actualizar(Long id, String nombreOriginal, MultipartFile archivo) throws IOException {
      
      Documento documento = idocumentoRepository.findById(id).orElse(null);
        if (documento == null) {
            return null;
        }

        if (archivo != null && !archivo.isEmpty()) {
            Path carpeta = Paths.get(uploadDir);
            Files.createDirectories(carpeta);

            Path archivoViejo = carpeta.resolve(documento.getNombreArchivo());
            Files.deleteIfExists(archivoViejo);

            String nombreOriginalArchivo = archivo.getOriginalFilename();
            String extension = "";
            if (nombreOriginalArchivo != null && nombreOriginalArchivo.contains(".")) {
                extension = nombreOriginalArchivo.substring(nombreOriginalArchivo.lastIndexOf("."));
            }
            String nuevoNombreArchivo = UUID.randomUUID() + extension;

            Files.copy(archivo.getInputStream(), carpeta.resolve(nuevoNombreArchivo));

            documento.setNombreArchivo(nuevoNombreArchivo);
            documento.setTamanoBytes(archivo.getSize());
            documento.setContentType(archivo.getContentType());
            documento.setFechaSubida(LocalDateTime.now());

            if (nombreOriginal == null || nombreOriginal.isBlank()) {
                documento.setNombreOriginal(nombreOriginalArchivo);
            }
        }

        if (nombreOriginal != null && !nombreOriginal.isBlank()) {
            documento.setNombreOriginal(nombreOriginal);
        }

        return idocumentoRepository.save(documento);
    }

    
}
