#!/bin/bash

# Esperar o Kafka iniciar
echo "Aguardando Kafka iniciar..."
sleep 30

# Definir o broker do Kafka
KAFKA_BROKER=kafka:9092

# Criar tópicos para cada serviço
echo "Criando tópicos do Kafka..."

# Tópicos para o serviço de catálogo
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic catalog-product-created --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic catalog-product-updated --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic catalog-product-deleted --partitions 3 --replication-factor 1

# Tópicos para o serviço de pedidos
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic order-created --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic order-updated --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic order-cancelled --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic order-completed --partitions 3 --replication-factor 1

# Tópicos para o serviço de carrinho
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic cart-created --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic cart-updated --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic cart-checkout --partitions 3 --replication-factor 1

# Tópicos para o serviço de usuário
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic user-created --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic user-updated --partitions 3 --replication-factor 1

# Tópicos para o serviço de pagamento
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic payment-processed --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic payment-failed --partitions 3 --replication-factor 1

# Tópicos para o serviço de inventário
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic inventory-updated --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic inventory-reserved --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic inventory-released --partitions 3 --replication-factor 1

# Tópicos para o serviço de notificações
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic notification-email --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic notification-sms --partitions 3 --replication-factor 1
kafka-topics --create --if-not-exists --bootstrap-server $KAFKA_BROKER --topic notification-push --partitions 3 --replication-factor 1

echo "Tópicos do Kafka criados com sucesso!"

# Listar todos os tópicos criados
echo "Listando todos os tópicos:"
kafka-topics --list --bootstrap-server $KAFKA_BROKER 