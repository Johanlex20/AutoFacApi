package com.AutoFacApi.services.iService;
import com.AutoFacApi.entity.Data;
import org.springframework.data.domain.Page;

public interface iDataService {

    public Page<Data> findAll(int pagina, int tamano);
    public Data save(Data data);
    public Data actualizar(Long id, Data data);
    public void eliminar(Long id);
}
