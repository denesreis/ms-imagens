package com.scasistemas.msbluedot.repository;

import com.scasistemas.msbluedot.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByCodigoErp(String codigoErp);

    boolean existsByCodigoErp(String codigoErp);
}
