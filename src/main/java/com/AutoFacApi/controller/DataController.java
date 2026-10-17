package com.AutoFacApi.controller;
import com.AutoFacApi.entity.Data;
import com.AutoFacApi.services.ExcelService;
import com.AutoFacApi.services.SeleniumService;
import com.AutoFacApi.services.iService.iDataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @Value("${demo.ocultar-datos-clientes:true}")
    private boolean ocultarDatosClientes;


    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    public Page<Data> findAll(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            Authentication authentication){
        Page<Data> resultado = dataService.findAll(pagina, tamano);

        if (ocultarDatosClientes && esDemo(authentication)) {
            return resultado.map(this::enmascarar);
        }
        return resultado;
    }

    private boolean esDemo(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(permiso -> "ROLE_DEMO".equals(permiso.getAuthority()));
    }

    // Devuelve una COPIA con cliente y NIT ocultos (nunca se modifica la entidad original).
    // El mismo cliente recibe siempre el mismo alias, asi las graficas siguen agrupando bien.
    private Data enmascarar(Data original) {
        Data copia = new Data();
        copia.setId(original.getId());
        copia.setNumeroFactura(original.getNumeroFactura());
        copia.setCliente("Cliente " + aliasDe(original.getCliente()));
        copia.setNit("Oculto en la demo");
        copia.setFechaGeneracion(original.getFechaGeneracion());
        copia.setFechaVencimiento(original.getFechaVencimiento());
        copia.setValorTotal(original.getValorTotal());
        copia.setFormaPago(original.getFormaPago());
        copia.setMedioPago(original.getMedioPago());
        return copia;
    }

    private String aliasDe(String cliente) {
        String clave = cliente == null ? "" : cliente.trim().toUpperCase();
        return String.format("%04d", Math.abs(clave.hashCode() % 10000));
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
