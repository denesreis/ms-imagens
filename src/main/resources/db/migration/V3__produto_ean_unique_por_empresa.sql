-- ============================================================
-- V3 - Ajuste de unicidade do EAN por empresa
-- Permite o mesmo EAN em empresas diferentes,
-- mas impede duplicidade dentro da mesma empresa.
-- ============================================================

-- Remove duplicatas de (codigo_ean, id_empresa), mantendo o registro mais recente
DELETE FROM tb_imagem
WHERE id_produto IN (
    SELECT id FROM tb_produto
    WHERE id NOT IN (
        SELECT DISTINCT ON (codigo_ean, id_empresa) id
        FROM tb_produto
        ORDER BY codigo_ean, id_empresa, data_criacao DESC
    )
);

DELETE FROM tb_produto
WHERE id NOT IN (
    SELECT DISTINCT ON (codigo_ean, id_empresa) id
    FROM tb_produto
    ORDER BY codigo_ean, id_empresa, data_criacao DESC
);

-- Remove o índice simples (sem unicidade)
DROP INDEX IF EXISTS idx_produto_codigo_ean;

-- Cria constraint composta UNIQUE (ean + empresa)
-- Permite o mesmo EAN em empresas distintas;
-- NULL id_empresa é tratado como distinto pelo PostgreSQL em UNIQUE.
ALTER TABLE tb_produto
    ADD CONSTRAINT uq_produto_ean_empresa UNIQUE (codigo_ean, id_empresa);

-- Recria índice simples para buscas por EAN
CREATE INDEX idx_produto_codigo_ean ON tb_produto (codigo_ean);
