package com.AutoFacApi.services;
import com.AutoFacApi.entity.Data;
import com.AutoFacApi.exception.RecursoNoEncontradoException;
import com.AutoFacApi.repository.iDataRepository;
import com.AutoFacApi.services.iService.iDataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DataService implements iDataService {

    @Autowired
    private iDataRepository idataRepository;

    @Override
    public Page<Data> findAll(int pagina, int tamano) {
        return idataRepository.findAll(PageRequest.of(pagina, tamano));
    }

    @Override
    public Data save(Data data) {
        if (idataRepository.existsByNumeroFactura(data.getNumeroFactura())) {
            throw new IllegalStateException("Ya existe un registro con el número de factura " + data.getNumeroFactura());
        }

        data.setId(null);
        return idataRepository.save(data);
    }

    @Override
    public Data actualizar(Long id, Data data) {
        Data existente = idataRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro no encontrado: " + id));

        boolean duplicado = idataRepository.findAllByNumeroFactura(data.getNumeroFactura())
                .stream()
                .anyMatch(otro -> !otro.getId().equals(id));

        if (duplicado) {
            throw new IllegalStateException("Ya existe otro registro con el número de factura " + data.getNumeroFactura());
        }

        existente.setNumeroFactura(data.getNumeroFactura());
        existente.setCliente(data.getCliente());
        existente.setNit(data.getNit());
        existente.setFechaGeneracion(data.getFechaGeneracion());
        existente.setFechaVencimiento(data.getFechaVencimiento());
        existente.setValorTotal(data.getValorTotal());
        existente.setFormaPago(data.getFormaPago());
        existente.setMedioPago(data.getMedioPago());

        return idataRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        idataRepository.deleteById(id);
    }
}
