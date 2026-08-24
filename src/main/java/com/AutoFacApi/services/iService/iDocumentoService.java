package com.AutoFacApi.services.iService;

import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.AutoFacApi.entity.Documento;

public interface iDocumentoService {
  List<Documento> findAll();
    Documento findById(Long id);
    Documento guardar(MultipartFile archivo) throws IOException;
}
