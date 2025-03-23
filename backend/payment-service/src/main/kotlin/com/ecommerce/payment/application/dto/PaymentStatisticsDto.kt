package com.ecommerce.payment.application.dto

import java.math.BigDecimal

/**
 * DTO para estatísticas de pagamentos
 * Usado no dashboard administrativo
 */
data class PaymentStatisticsDto(
    // Total de transações no período
    val totalTransactions: Long,
    
    // Valor total de transações no período
    val totalAmount: BigDecimal,
    
    // Transações bem-sucedidas
    val successfulTransactions: Long,
    val successfulAmount: BigDecimal,
    
    // Transações com falha
    val failedTransactions: Long,
    
    // Transações canceladas
    val canceledTransactions: Long,
    
    // Transações reembolsadas
    val refundedTransactions: Long,
    val refundedAmount: BigDecimal,
    
    // Transações pendentes
    val pendingTransactions: Long,
    val pendingAmount: BigDecimal,
    
    // Período da consulta
    val period: String
) {
    // Taxa de sucesso (percentual)
    val successRate: Double
        get() = if (totalTransactions > 0) {
            (successfulTransactions.toDouble() / totalTransactions.toDouble()) * 100.0
        } else {
            0.0
        }
    
    // Taxa de falha (percentual)
    val failureRate: Double
        get() = if (totalTransactions > 0) {
            (failedTransactions.toDouble() / totalTransactions.toDouble()) * 100.0
        } else {
            0.0
        }
    
    // Taxa de cancelamento (percentual)
    val cancellationRate: Double
        get() = if (totalTransactions > 0) {
            (canceledTransactions.toDouble() / totalTransactions.toDouble()) * 100.0
        } else {
            0.0
        }
    
    // Taxa de reembolso (percentual)
    val refundRate: Double
        get() = if (totalTransactions > 0) {
            (refundedTransactions.toDouble() / totalTransactions.toDouble()) * 100.0
        } else {
            0.0
        }
} 