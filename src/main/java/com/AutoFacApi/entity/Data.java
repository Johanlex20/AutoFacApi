package com.AutoFacApi.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "Data")
public class Data {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String numeroFactura;
    private String cliente;
    private String nit;
    private String fechaGeneracion;
    private String fechaVencimiento;
    private String valorTotal;
    private String formaPago;
    private String MedioPago;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(String fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(String valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public String getMedioPago() {
        return MedioPago;
    }

    public void setMedioPago(String medioPago) {
        MedioPago = medioPago;
    }
}
