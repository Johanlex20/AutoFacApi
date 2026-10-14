package com.AutoFacApi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Guarda, sirve y borra imagenes subidas (productos, fotos de perfil...) dentro de
 * {documentos.upload-dir}/{subcarpeta}. Comparte una sola validacion para todas.
 */
@Service
public class ImagenService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[0-9a-f-]{36}\\.(jpg|png|webp|gif)$");

    @Value("${documentos.upload-dir}")
    private String uploadDir;

    /** Valida y guarda la imagen; devuelve el nombre de archivo generado (UUID + extension). */
    public String guardar(String subcarpeta, MultipartFile imagen) throws IOException {
        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException("Selecciona una imagen.");
        }
        if (imagen.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("La imagen no puede superar 5 MB.");
        }

        byte[] contenido = imagen.getBytes();
        // Se decide por el contenido real del archivo, no por el nombre ni el content-type que manda el navegador
        String extension = detectarExtension(contenido);
        if (extension == null) {
            throw new IllegalArgumentException("Formato no permitido. Usa una imagen JPG, PNG, WEBP o GIF.");
        }

        Path carpeta = carpeta(subcarpeta);
        Files.createDirectories(carpeta);

        String nombre = UUID.randomUUID() + "." + extension;
        Files.write(carpeta.resolve(nombre), contenido);
        return nombre;
    }

    /** Devuelve el archivo, o null si el nombre no es uno de los generados aqui o no existe. */
    public Resource cargar(String subcarpeta, String nombre) throws IOException {
        // Solo nombres que generamos nosotros: evita rutas tipo ../ o archivos ajenos
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            return null;
        }
        Path archivo = carpeta(subcarpeta).resolve(nombre);
        if (!Files.isRegularFile(archivo)) {
            return null;
        }
        return new UrlResource(archivo.toUri());
    }

    public void borrar(String subcarpeta, String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(carpeta(subcarpeta).resolve(nombre));
        } catch (IOException e) {
            // Un archivo huerfano no debe impedir actualizar o borrar el registro
        }
    }

    public static MediaType tipoPorNombre(String nombre) {
        if (nombre.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (nombre.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (nombre.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    private Path carpeta(String subcarpeta) {
        return Paths.get(uploadDir).resolve(subcarpeta);
    }

    private String detectarExtension(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a') {
            return "gif";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }
}
