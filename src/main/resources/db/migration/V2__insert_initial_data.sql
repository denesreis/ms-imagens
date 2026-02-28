-- ============================================================
-- V2 - Dados iniciais
-- ms-imagens - Flyway Migration
-- ============================================================
-- ATENÇÃO: a senha '$2a$10$...' abaixo é um PLACEHOLDER.
-- Substitua por um hash BCrypt real antes de usar em produção.
-- Gere com: BCrypt.hashpw("suaSenhaAqui", BCrypt.gensalt(10))
-- ============================================================

-- Empresa padrão
INSERT INTO tb_empresa (codigo_erp, nome, ativo, data_criacao, data_atualizacao)
VALUES ('001', 'Empresa Padrão', TRUE, NOW(), NOW());

-- Usuário administrador padrão
-- Senha: trocar OBRIGATORIAMENTE antes do primeiro deploy
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
    '$2a$12$rTsw9BMAEfghkdBwTehWcOkVHNorQCpURhMF793Z9nrAlWJrS5V3S',
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
