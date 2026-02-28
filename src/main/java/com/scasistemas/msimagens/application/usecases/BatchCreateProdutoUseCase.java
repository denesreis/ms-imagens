package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.imagem.ImagemUploadRequest;
import com.scasistemas.msimagens.application.dto.produto.BatchProdutoItemResult;
import com.scasistemas.msimagens.application.dto.produto.BatchProdutoResponse;
import com.scasistemas.msimagens.application.dto.produto.ProdutoRequest;
import com.scasistemas.msimagens.application.dto.produto.ProdutoResponse;
import com.scasistemas.msimagens.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msimagens.infrastructure.eanpictures.EanPicturesResult;
import com.scasistemas.msimagens.infrastructure.eanpictures.EanPicturesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Use Case para cadastro em lote de produtos com busca automática de imagem
 * na API pública eanpictures.com.br.
 *
 * <p>
 * Fluxo por item:
 * </p>
 * <ol>
 * <li>Busca imagem em
 * {@code http://eanpictures.com.br:9000/api/gtin/{ean}}</li>
 * <li>Se imagem NÃO encontrada → item retorna {@code SEM_IMAGEM} e produto NÃO
 * é cadastrado</li>
 * <li>Cria o produto (reutiliza {@link CreateProdutoUseCase})</li>
 * <li>Faz upload da imagem para a Cloudflare e vincula ao produto</li>
 * </ol>
 *
 * <p>
 * Erros individuais (EAN duplicado, timeout de rede etc.) não interrompem
 * os demais itens do lote — cada item tem seu {@link BatchProdutoItemResult}
 * com status {@code CRIADO}, {@code IGNORADO}, {@code SEM_IMAGEM} ou
 * {@code ERRO}.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchCreateProdutoUseCase {

    private final CreateProdutoUseCase createProdutoUseCase;
    private final CreateImagemUseCase createImagemUseCase;
    private final EanPicturesService eanPicturesService;

    /**
     * Processa a lista de produtos.
     *
     * @param produtos lista de {@link ProdutoRequest} a cadastrar
     * @return {@link BatchProdutoResponse} com resumo e detalhes de cada item
     */
    public BatchProdutoResponse execute(List<ProdutoRequest> produtos) {
        log.info("[BatchCreateProduto] Iniciando lote com {} produto(s)", produtos.size());

        List<BatchProdutoItemResult> resultados = new ArrayList<>();
        int criados = 0, erros = 0, ignorados = 0, semImagem = 0;

        for (ProdutoRequest request : produtos) {
            BatchProdutoItemResult resultado = processarItem(request);
            resultados.add(resultado);

            switch (resultado.getStatus()) {
                case "CRIADO" -> criados++;
                case "IGNORADO" -> ignorados++;
                case "SEM_IMAGEM" -> semImagem++;
                default -> erros++;
            }
        }

        log.info("[BatchCreateProduto] Lote finalizado: criados={} semImagem={} erros={} ignorados={}",
                criados, semImagem, erros, ignorados);

        return BatchProdutoResponse.builder()
                .total(produtos.size())
                .criados(criados)
                .erros(erros)
                .ignorados(ignorados)
                .semImagem(semImagem)
                .resultados(resultados)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Processamento individual
    // ─────────────────────────────────────────────────────────────────────────

    private BatchProdutoItemResult processarItem(ProdutoRequest request) {
        String ean = request.getCodigoEan();
        log.debug("[BatchCreateProduto] Processando EAN={}", ean);

        // 1. Busca imagem na API eanpictures (não bloqueia o cadastro em caso de falha)
        boolean imagemEncontrada = false;
        boolean imagemCarregada = false;
        Optional<EanPicturesResult> imagemOpt = Optional.empty();

        if (ean != null && !ean.isBlank()) {
            imagemOpt = eanPicturesService.fetchImage(ean);
            imagemEncontrada = imagemOpt.isPresent();
        }

        // 1b. Imagem não encontrada → produto NÃO é cadastrado
        if (imagemOpt.isEmpty()) {
            log.info("[BatchCreateProduto] EAN={} sem imagem no eanpictures — produto não cadastrado", ean);
            return BatchProdutoItemResult.builder()
                    .codigoEan(ean)
                    .status("SEM_IMAGEM")
                    .mensagem("Imagem não encontrada em eanpictures.com.br — produto não foi cadastrado")
                    .imagemEncontrada(false)
                    .imagemCarregada(false)
                    .build();
        }

        // 2. Tenta criar o produto (imagem já garantida)
        ProdutoResponse produtoCriado;
        try {
            produtoCriado = createProdutoUseCase.execute(request);
        } catch (com.scasistemas.msimagens.domain.exceptions.BusinessException ex) {
            // EAN duplicado ou outra regra de negócio → ignora sem erro crítico
            log.info("[BatchCreateProduto] EAN={} ignorado: {}", ean, ex.getMessage());
            return BatchProdutoItemResult.builder()
                    .codigoEan(ean)
                    .status("IGNORADO")
                    .mensagem(ex.getMessage())
                    .imagemEncontrada(imagemEncontrada)
                    .imagemCarregada(false)
                    .build();
        } catch (Exception ex) {
            log.error("[BatchCreateProduto] EAN={} erro inesperado: {}", ean, ex.getMessage());
            return BatchProdutoItemResult.builder()
                    .codigoEan(ean)
                    .status("ERRO")
                    .mensagem("Erro ao criar produto: " + ex.getMessage())
                    .imagemEncontrada(imagemEncontrada)
                    .imagemCarregada(false)
                    .build();
        }

        // 3. Upload da imagem (garantida não-vazia neste ponto)
        EanPicturesResult img = imagemOpt.get();
        try {
            ImagemUploadRequest uploadRequest = ImagemUploadRequest.builder()
                    .idProduto(produtoCriado.getId())
                    .idEmpresa(produtoCriado.getIdEmpresa())
                    .tipoArmazenamento(TipoArmazenamentoEnum.ABERTO)
                    .build();
            var imagemResponse = createImagemUseCase.executeFromBytes(
                    img.bytes(), img.filename(), img.contentType(), uploadRequest);
            produtoCriado.setImagens(List.of(imagemResponse));
            imagemCarregada = true;
            log.info("[BatchCreateProduto] EAN={} imagem carregada para Cloudflare", ean);
        } catch (Exception ex) {
            // Produto criado, mas upload falhou — registra aviso
            log.warn("[BatchCreateProduto] EAN={} produto criado mas upload falhou: {}", ean, ex.getMessage());
        }

        String mensagem = imagemCarregada
                ? "Produto criado com imagem da eanpictures.com.br"
                : "Produto criado, mas falha no upload da imagem para a Cloudflare";

        return BatchProdutoItemResult.builder()
                .codigoEan(ean)
                .status("CRIADO")
                .mensagem(mensagem)
                .imagemEncontrada(imagemEncontrada)
                .imagemCarregada(imagemCarregada)
                .produto(produtoCriado)
                .build();
    }
}
