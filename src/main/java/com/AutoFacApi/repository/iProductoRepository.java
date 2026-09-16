package com.AutoFacApi.repository;

import com.AutoFacApi.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface iProductoRepository extends JpaRepository<Producto, Long> {
}
