package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.imagem.ImagemProdutoResponse;
import com.scasistemas.msimagens.application.mappers.ImagemMapper;
import com.scasistemas.msimagens.config.CacheConfig;
import com.scasistemas.msimagens.domain.entities.Imagem;
import com.scasistemas.msimagens.domain.repositories.IImagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case para consulta pública de imagens por código EAN.
 *
 * <p>
 * Retorna apenas imagens com {@code tipoArmazenamento = ABERTO} e
 * {@code status = ATIVO}. Endpoint público (sem autenticação JWT).
 * Resultado armazenado em cache Caffeine para reduzir carga no banco.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetImagensByCodigoEanUseCase {

    private final IImagemRepository imagemRepository;
    private final ImagemMapper imagemMapper;

    /**
     * Busca imagens públicas pelo código EAN do produto.
     * Resultado em cache por 31 minutos (TTL do {@code imagensPorEan}).
     *
     * @param codigoEan código de barras EAN-13
     * @return lista de imagens públicas (pode ser vazia)
     */
    @Cacheable(value = CacheConfig.CACHE_IMAGENS_POR_EAN, key = "#codigoEan")
    @Transactional(readOnly = true)
    public List<ImagemProdutoResponse> execute(String codigoEan) {
        log.debug("[GetImagensByEan] Buscando imagens para ean='{}'", codigoEan);

        List<Imagem> imagens = imagemRepository.findByCodigoEanAndTipoAberto(codigoEan);

        List<ImagemProdutoResponse> response = imagens.stream()
                .map(imagemMapper::toProdutoResponse)
                .toList();

        log.debug("[GetImagensByEan] {} imagens encontradas para ean='{}'", response.size(), codigoEan);
        return response;
    }
}
