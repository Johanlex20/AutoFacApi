package com.AutoFacApi.controller;
import com.AutoFacApi.entity.Data;
import com.AutoFacApi.services.ExcelService;
import com.AutoFacApi.services.SeleniumService;
import com.AutoFacApi.services.iDataService.iDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/data")
public class DataController {

    @Autowired
    iDataService dataService;

    @Autowired
    private SeleniumService seleniumService;

    @Autowired
    private ExcelService excelService;


    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    public List<Data> findAll(){
        return dataService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Data save(@RequestBody Data data){
        return dataService.save(data);
    }

    @PostMapping("/capturar")
    public String capturar() throws Exception {

        Data data = seleniumService.capturarFactura();

        dataService.save(data);

        excelService.guardar(data);

        return "Factura guardada correctamente";
    }



}
