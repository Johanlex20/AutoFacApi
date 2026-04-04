package com.AutoFacApi.services;
import com.AutoFacApi.entity.Data;
import com.AutoFacApi.repository.iDataRepository;
import com.AutoFacApi.services.iDataService.iDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DataService implements iDataService {

    @Autowired
    private iDataRepository idataRepository;

    @Override
    public List<Data> findAll() {
        return idataRepository.findAll();
    }

    @Override
    public Data save(Data data) {
        return idataRepository.save(data);
    }
}
