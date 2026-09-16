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

import net.coobird.thumbnailator.Thumbnails;


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

        ArchivoGuardado guardado = guardarArchivo(archivo, carpeta);

        Documento documento = new Documento();
        documento.setNombreOriginal(archivo.getOriginalFilename());
        documento.setNombreArchivo(guardado.nombreArchivo());
        documento.setTamanoBytes(guardado.tamanoBytes());
        documento.setContentType(guardado.contentType());
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

            ArchivoGuardado guardado = guardarArchivo(archivo, carpeta);

            documento.setNombreArchivo(guardado.nombreArchivo());
            documento.setTamanoBytes(guardado.tamanoBytes());
            documento.setContentType(guardado.contentType());
            documento.setFechaSubida(LocalDateTime.now());

            if (nombreOriginal == null || nombreOriginal.isBlank()) {
                documento.setNombreOriginal(archivo.getOriginalFilename());
            }
        }

        if (nombreOriginal != null && !nombreOriginal.isBlank()) {
            documento.setNombreOriginal(nombreOriginal);
        }

        return idocumentoRepository.save(documento);
    }

    /**
     * Si el archivo es una imagen, la re-codifica como JPG optimizado (reduce peso).
     * Cualquier otro tipo de archivo se guarda tal cual.
     */
    private ArchivoGuardado guardarArchivo(MultipartFile archivo, Path carpeta) throws IOException {
        boolean esImagen = esImagen(archivo.getContentType());
        String extension = esImagen ? ".jpg" : obtenerExtension(archivo.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID() + extension;
        Path destino = carpeta.resolve(nombreArchivo);

        if (esImagen) {
            // Limita a 1920px de lado más largo (no agranda imágenes ya pequeñas) y comprime al 80%
            Thumbnails.of(archivo.getInputStream())
                    .size(1920, 1920)
                    .outputFormat("jpg")
                    .outputQuality(0.8)
                    .toFile(destino.toFile());
            return new ArchivoGuardado(nombreArchivo, Files.size(destino), "image/jpeg");
        }

        Files.copy(archivo.getInputStream(), destino);
        return new ArchivoGuardado(nombreArchivo, archivo.getSize(), archivo.getContentType());
    }

    private boolean esImagen(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo != null && nombreArchivo.contains(".")) {
            return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
        }
        return "";
    }

    private record ArchivoGuardado(String nombreArchivo, long tamanoBytes, String contentType) {}
}
