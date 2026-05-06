-- ============================================================
-- V4 - Corrige unicidade do EAN para suportar soft delete
-- O UNIQUE constraint simples bloqueia re-criação de produtos
-- após soft delete (ativo=false). Substitui por partial index
-- que só considera registros ativos.
-- ============================================================

-- Remove a constraint UNIQUE simples criada na V3
ALTER TABLE tb_produto
    DROP CONSTRAINT uq_produto_ean_empresa;

-- Cria índice único parcial: só aplica a produtos ativos
-- Permite re-criar um produto com mesmo EAN após soft delete
CREATE UNIQUE INDEX uq_produto_ativo_ean_empresa
    ON tb_produto (codigo_ean, id_empresa)
    WHERE ativo = true;
