-- Criação da tabela de métodos de pagamento
CREATE TABLE payment_methods (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    provider_token_id VARCHAR(255) NOT NULL,
    last4_digits VARCHAR(4) NOT NULL,
    expiry_month INT,
    expiry_year INT,
    card_brand VARCHAR(50),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Índices para a tabela de métodos de pagamento
CREATE INDEX idx_payment_methods_user_id ON payment_methods(user_id);
CREATE INDEX idx_payment_methods_provider_token_id ON payment_methods(provider_token_id);
CREATE INDEX idx_payment_methods_user_default ON payment_methods(user_id, is_default);

-- Criação da tabela de pagamentos
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_intent_id VARCHAR(255),
    payment_method_id VARCHAR(255),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- Índices para a tabela de pagamentos
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_intent_id ON payments(payment_intent_id);

-- Comentários para documentação das tabelas
COMMENT ON TABLE payment_methods IS 'Armazena os métodos de pagamento dos usuários';
COMMENT ON TABLE payments IS 'Armazena os pagamentos realizados pelos usuários';

-- Comentários para documentação das colunas
COMMENT ON COLUMN payment_methods.id IS 'ID único do método de pagamento';
COMMENT ON COLUMN payment_methods.user_id IS 'ID do usuário proprietário do método de pagamento';
COMMENT ON COLUMN payment_methods.type IS 'Tipo do método de pagamento (CREDIT_CARD, DEBIT_CARD, etc.)';
COMMENT ON COLUMN payment_methods.provider_token_id IS 'ID do token no provedor de pagamento (ex: Stripe)';
COMMENT ON COLUMN payment_methods.last4_digits IS 'Últimos 4 dígitos do cartão';
COMMENT ON COLUMN payment_methods.expiry_month IS 'Mês de expiração do cartão';
COMMENT ON COLUMN payment_methods.expiry_year IS 'Ano de expiração do cartão';
COMMENT ON COLUMN payment_methods.card_brand IS 'Bandeira do cartão (Visa, Mastercard, etc.)';
COMMENT ON COLUMN payment_methods.is_default IS 'Indica se este é o método de pagamento padrão do usuário';
COMMENT ON COLUMN payment_methods.created_at IS 'Data e hora de criação do registro';
COMMENT ON COLUMN payment_methods.updated_at IS 'Data e hora da última atualização do registro';

COMMENT ON COLUMN payments.id IS 'ID único do pagamento';
COMMENT ON COLUMN payments.order_id IS 'ID do pedido associado ao pagamento';
COMMENT ON COLUMN payments.user_id IS 'ID do usuário que realizou o pagamento';
COMMENT ON COLUMN payments.amount IS 'Valor do pagamento';
COMMENT ON COLUMN payments.currency IS 'Moeda do pagamento (ex: BRL, USD)';
COMMENT ON COLUMN payments.status IS 'Status do pagamento (PENDING, PROCESSING, COMPLETED, etc.)';
COMMENT ON COLUMN payments.payment_intent_id IS 'ID da intenção de pagamento no provedor (ex: Stripe)';
COMMENT ON COLUMN payments.payment_method_id IS 'ID do método de pagamento utilizado';
COMMENT ON COLUMN payments.error_message IS 'Mensagem de erro em caso de falha no pagamento';
COMMENT ON COLUMN payments.created_at IS 'Data e hora de criação do pagamento';
COMMENT ON COLUMN payments.updated_at IS 'Data e hora da última atualização do pagamento';
COMMENT ON COLUMN payments.completed_at IS 'Data e hora em que o pagamento foi concluído'; 