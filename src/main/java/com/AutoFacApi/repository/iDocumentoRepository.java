package com.AutoFacApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.AutoFacApi.entity.Documento;

@Repository
public interface iDocumentoRepository extends JpaRepository<Documento, Long> {
  
}
