package com.AutoFacApi.repository;
import com.AutoFacApi.entity.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface iDataRepository extends JpaRepository<Data, Long> {
    boolean existsByNumeroFactura(String numeroFactura);
    List<Data> findAllByNumeroFactura(String numeroFactura);
}
