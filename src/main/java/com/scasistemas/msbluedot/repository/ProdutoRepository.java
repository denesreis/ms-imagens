package com.scasistemas.msbluedot.repository;

import com.scasistemas.msbluedot.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {

    List<Produto> findByCodigoEan(String codigoEan);

    boolean existsByCodigoEanAndIdEmpresa(String codigoEan, Long idEmpresa);

    boolean existsByCodigoEanAndIdEmpresaIsNull(String codigoEan);

    List<Produto> findByIdEmpresa(Long idEmpresa);
}
