package com.AutoFacApi.controller;
import com.AutoFacApi.entity.Data;
import com.AutoFacApi.services.ExcelService;
import com.AutoFacApi.services.SeleniumService;
import com.AutoFacApi.services.iService.iDataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public Page<Data> findAll(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano){
        return dataService.findAll(pagina, tamano);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Data save(@RequestBody Data data){
        return dataService.save(data);
    }

    @PutMapping("/{id}")
    public Data actualizar(@PathVariable Long id, @RequestBody Data data) {
        return dataService.actualizar(id, data);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        dataService.eliminar(id);
    }

    @PostMapping("/capturar")
    public ResponseEntity<Map<String, Object>> capturar() throws Exception {

        Data data = seleniumService.capturarFactura();

        dataService.save(data);

        excelService.guardar(data);

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje","Factura guardada correctamente");
        response.put("success", true);

        return ResponseEntity.ok(response);
    }



}
