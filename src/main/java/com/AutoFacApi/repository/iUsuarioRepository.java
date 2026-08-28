package com.AutoFacApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.AutoFacApi.entity.Usuario;
import java.util.Optional;


@Repository
public interface iUsuarioRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByEmail(String email);
}
