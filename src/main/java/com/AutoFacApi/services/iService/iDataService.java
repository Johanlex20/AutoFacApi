package com.AutoFacApi.services.iService;
import com.AutoFacApi.entity.Data;
import java.util.List;

public interface iDataService {

    public List<Data>findAll();
    public Data save(Data data);
}
