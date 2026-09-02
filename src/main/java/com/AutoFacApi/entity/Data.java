package com.AutoFacApi.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}
