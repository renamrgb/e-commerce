package com.ecommerce.order.domain.exception

/**
 * Exceção lançada quando um pedido não é encontrado
 */
class OrderNotFoundException(message: String) : RuntimeException(message) 