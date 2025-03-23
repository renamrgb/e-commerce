-- Tabela para o padrão Outbox para garantir a entrega de eventos
CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    message_key VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retries INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP
);

-- Criação de índices para otimizar consultas
CREATE INDEX idx_outbox_status ON outbox_events (status);
CREATE INDEX idx_outbox_aggregate ON outbox_events (aggregate_type, aggregate_id);
CREATE INDEX idx_outbox_created_at ON outbox_events (created_at);

-- Comentários nas colunas para documentação
COMMENT ON TABLE outbox_events IS 'Armazena eventos a serem publicados no Kafka usando o padrão Outbox';
COMMENT ON COLUMN outbox_events.id IS 'Identificador único do evento';
COMMENT ON COLUMN outbox_events.aggregate_id IS 'Identificador da entidade relacionada ao evento (ex: ID do pagamento)';
COMMENT ON COLUMN outbox_events.aggregate_type IS 'Tipo da entidade relacionada ao evento (ex: payment, payment_method)';
COMMENT ON COLUMN outbox_events.event_type IS 'Tipo do evento (ex: completed, failed, canceled)';
COMMENT ON COLUMN outbox_events.topic IS 'Tópico Kafka para onde o evento deve ser enviado';
COMMENT ON COLUMN outbox_events.message_key IS 'Chave da mensagem Kafka';
COMMENT ON COLUMN outbox_events.payload IS 'Conteúdo serializado do evento em JSON';
COMMENT ON COLUMN outbox_events.status IS 'Status do processamento (PENDING, PROCESSING, PROCESSED, FAILED)';
COMMENT ON COLUMN outbox_events.retries IS 'Número de tentativas de envio do evento';
COMMENT ON COLUMN outbox_events.error_message IS 'Mensagem de erro da última tentativa falha';
COMMENT ON COLUMN outbox_events.created_at IS 'Data e hora de criação do evento';
COMMENT ON COLUMN outbox_events.updated_at IS 'Data e hora da última atualização do evento';
COMMENT ON COLUMN outbox_events.processed_at IS 'Data e hora do processamento bem-sucedido do evento'; 