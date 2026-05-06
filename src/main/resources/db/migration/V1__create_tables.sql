-- ============================================================
-- V1 - Criação das tabelas principais
-- ms-bluedot - Flyway Migration
-- ============================================================

-- ============================================================
-- tb_empresa
-- ============================================================
CREATE TABLE tb_empresa (
    id               BIGSERIAL       PRIMARY KEY,
    codigo_erp       VARCHAR(20)     NOT NULL,
    nome             VARCHAR(100)    NOT NULL,
    ativo            BOOLEAN         NOT NULL DEFAULT TRUE,
    data_criacao     TIMESTAMP       NOT NULL,
    data_atualizacao TIMESTAMP       NOT NULL,

    CONSTRAINT uq_empresa_codigo_erp UNIQUE (codigo_erp)
);

CREATE INDEX idx_empresa_codigo_erp ON tb_empresa (codigo_erp);
CREATE INDEX idx_empresa_ativo      ON tb_empresa (ativo);

-- ============================================================
-- tb_usuario
-- ============================================================
CREATE TABLE tb_usuario (
    id               BIGSERIAL       PRIMARY KEY,
    id_empresa       BIGINT          REFERENCES tb_empresa (id),
    nome             VARCHAR(30)     NOT NULL,
    senha            VARCHAR(60)     NOT NULL,
    role             VARCHAR(20)     NOT NULL,
    ativo            BOOLEAN         NOT NULL DEFAULT TRUE,
    tentativas_login INTEGER         NOT NULL DEFAULT 0,
    bloqueado_ate    TIMESTAMP,
    data_criacao     TIMESTAMP       NOT NULL,
    data_atualizacao TIMESTAMP       NOT NULL,

    CONSTRAINT uq_usuario_nome UNIQUE (nome)
);

CREATE UNIQUE INDEX idx_usuario_nome      ON tb_usuario (nome);
CREATE        INDEX idx_usuario_id_empresa ON tb_usuario (id_empresa);
CREATE        INDEX idx_usuario_ativo      ON tb_usuario (ativo);

-- ============================================================
-- tb_produto
-- ============================================================
CREATE TABLE tb_produto (
    id               VARCHAR(36)     PRIMARY KEY,
    descricao        VARCHAR(80)     NOT NULL,
    discriminacao    VARCHAR(200),
    codigo_erp       VARCHAR(20),
    id_empresa       BIGINT          REFERENCES tb_empresa (id),
    codigo_ean       VARCHAR(13)     NOT NULL,
    ativo            BOOLEAN         NOT NULL DEFAULT TRUE,
    data_criacao     TIMESTAMP       NOT NULL,
    data_atualizacao TIMESTAMP       NOT NULL
);

CREATE INDEX idx_produto_codigo_ean ON tb_produto (codigo_ean);
CREATE INDEX idx_produto_codigo_erp ON tb_produto (codigo_erp);
CREATE INDEX idx_produto_id_empresa ON tb_produto (id_empresa);
CREATE INDEX idx_produto_ativo       ON tb_produto (ativo);

-- ============================================================
-- tb_imagem
-- ============================================================
CREATE TABLE tb_imagem (
    id                    VARCHAR(36)     PRIMARY KEY,
    id_produto            VARCHAR(36)     NOT NULL REFERENCES tb_produto (id),
    id_empresa            BIGINT          REFERENCES tb_empresa (id),
    tipo_armazenamento    VARCHAR(10)     NOT NULL,
    id_imagem_cloudflare  VARCHAR(50),
    url                   TEXT,
    filename              VARCHAR(255),
    status                VARCHAR(10)     NOT NULL DEFAULT 'PENDENTE',
    ativo                 BOOLEAN         NOT NULL DEFAULT TRUE,
    data_criacao          TIMESTAMP       NOT NULL,
    data_atualizacao      TIMESTAMP       NOT NULL,

    CONSTRAINT chk_imagem_status              CHECK (status IN ('PENDENTE', 'ATIVO', 'ERRO')),
    CONSTRAINT chk_imagem_tipo_armazenamento  CHECK (tipo_armazenamento IN ('ABERTO', 'PRIVADO'))
);

CREATE INDEX idx_imagem_id_produto        ON tb_imagem (id_produto);
CREATE INDEX idx_imagem_id_empresa        ON tb_imagem (id_empresa);
CREATE INDEX idx_imagem_status            ON tb_imagem (status);
CREATE INDEX idx_imagem_tipo_armazenamento ON tb_imagem (tipo_armazenamento);
CREATE INDEX idx_imagem_ativo             ON tb_imagem (ativo);

-- ============================================================
-- tb_refresh_token
-- ============================================================
CREATE TABLE tb_refresh_token (
    id           BIGSERIAL       PRIMARY KEY,
    id_usuario   BIGINT          NOT NULL REFERENCES tb_usuario (id),
    token_hash   VARCHAR(255)    NOT NULL,
    expira_em    TIMESTAMP       NOT NULL,
    revogado     BOOLEAN         NOT NULL DEFAULT FALSE,
    data_criacao TIMESTAMP       NOT NULL,

    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash)
);

CREATE UNIQUE INDEX idx_refresh_token_hash     ON tb_refresh_token (token_hash);
CREATE        INDEX idx_refresh_token_usuario  ON tb_refresh_token (id_usuario);
CREATE        INDEX idx_refresh_token_expira_em ON tb_refresh_token (expira_em);

-- ============================================================
-- tb_audit_log
-- ============================================================
CREATE TABLE tb_audit_log (
    id           BIGSERIAL       PRIMARY KEY,
    acao         VARCHAR(50)     NOT NULL,
    usuario      VARCHAR(30)     NOT NULL,
    id_empresa   BIGINT,
    ip           VARCHAR(45),
    detalhes     TEXT,
    data_criacao TIMESTAMP       NOT NULL
);

CREATE INDEX idx_audit_log_acao        ON tb_audit_log (acao);
CREATE INDEX idx_audit_log_usuario     ON tb_audit_log (usuario);
CREATE INDEX idx_audit_log_data_criacao ON tb_audit_log (data_criacao);
CREATE INDEX idx_audit_log_id_empresa  ON tb_audit_log (id_empresa);
