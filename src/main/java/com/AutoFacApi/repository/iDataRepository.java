package com.AutoFacApi.repository;
import com.AutoFacApi.entity.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface iDataRepository extends JpaRepository<Data, Long> {
}
