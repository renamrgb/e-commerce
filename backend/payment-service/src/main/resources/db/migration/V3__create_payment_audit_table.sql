-- Cria tabela de auditoria de pagamentos
CREATE TABLE payment_audits (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    message VARCHAR(500),
    actor_id VARCHAR(36),
    source VARCHAR(20) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL
);

-- Cria índices para otimizar consultas
CREATE INDEX idx_payment_audits_payment_id ON payment_audits(payment_id);
CREATE INDEX idx_payment_audits_created_at ON payment_audits(created_at);
CREATE INDEX idx_payment_audits_source ON payment_audits(source);
CREATE INDEX idx_payment_audits_actor_id ON payment_audits(actor_id);

-- Comentários na tabela e colunas para documentação
COMMENT ON TABLE payment_audits IS 'Registros de auditoria para mudanças de status de pagamentos';
COMMENT ON COLUMN payment_audits.id IS 'Identificador único da auditoria';
COMMENT ON COLUMN payment_audits.payment_id IS 'Referência ao ID do pagamento';
COMMENT ON COLUMN payment_audits.previous_status IS 'Status anterior do pagamento';
COMMENT ON COLUMN payment_audits.new_status IS 'Novo status do pagamento';
COMMENT ON COLUMN payment_audits.message IS 'Mensagem descritiva sobre a mudança';
COMMENT ON COLUMN payment_audits.actor_id IS 'ID do usuário que realizou a ação (se aplicável)';
COMMENT ON COLUMN payment_audits.source IS 'Origem da mudança (SYSTEM, WEBHOOK, USER, API, SCHEDULED)';
COMMENT ON COLUMN payment_audits.metadata IS 'Metadados adicionais em formato JSON';
COMMENT ON COLUMN payment_audits.created_at IS 'Data e hora do registro da auditoria'; 