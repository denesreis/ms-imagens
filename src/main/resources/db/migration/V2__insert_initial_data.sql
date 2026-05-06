-- ============================================================
-- V2 - Dados iniciais
-- ms-bluedot - Flyway Migration
-- ============================================================
-- Senha inicial: senha123 (BCrypt 12 rounds)
-- ATENÇÃO: trocar OBRIGATORIAMENTE antes do primeiro deploy em produção.
-- Gere novo hash com: BCrypt.hashpw("novaSenha", BCrypt.gensalt(12))
-- ============================================================

-- Empresa padrão
INSERT INTO tb_empresa (codigo_erp, nome, ativo, data_criacao, data_atualizacao)
VALUES ('001', 'Empresa Padrão', TRUE, NOW(), NOW());

-- Usuário administrador padrão
-- Senha: senha123 — trocar OBRIGATORIAMENTE antes do primeiro deploy em produção
INSERT INTO tb_usuario (
    id_empresa,
    nome,
    senha,
    role,
    ativo,
    tentativas_login,
    bloqueado_ate,
    data_criacao,
    data_atualizacao
)
VALUES (
    1,
    'admin',
    '$2a$12$B7MDI1Ta2L3l2dpBsXbOQefgE/ZC5FOcGzQOiQuWxs14b2VDixPtC',
    'ADMINISTRADOR',
    TRUE,
    0,
    NULL,
    NOW(),
    NOW()
);

-- Registro de auditoria do seed
INSERT INTO tb_audit_log (acao, usuario, id_empresa, ip, detalhes, data_criacao)
VALUES ('SEED_DADOS_INICIAIS', 'flyway', 1, '127.0.0.1', 'Migração V2: dados iniciais inseridos', NOW());
