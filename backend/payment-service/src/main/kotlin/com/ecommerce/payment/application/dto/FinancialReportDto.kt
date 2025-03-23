package com.ecommerce.payment.application.dto

import com.ecommerce.payment.domain.model.PaymentStatus
import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTO para relatórios financeiros
 */
data class FinancialReportDto(
    // Período do relatório
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startDate: LocalDate,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    val endDate: LocalDate,
    
    // Resumo
    val totalTransactions: Int,
    val totalAmount: BigDecimal,
    val avgOrderValue: BigDecimal,
    
    // Análise por status
    val completedTransactions: Int,
    val completedAmount: BigDecimal,
    val failedTransactions: Int,
    val failedAmount: BigDecimal,
    val pendingTransactions: Int,
    val pendingAmount: BigDecimal,
    val refundedTransactions: Int,
    val refundedAmount: BigDecimal,
    val canceledTransactions: Int,
    val canceledAmount: BigDecimal,
    
    // Taxa de sucesso
    val successRate: Double,
    
    // Análise diária
    val dailyStats: List<DailyStatsDto>,
    
    // Metadados do relatório
    val generatedAt: LocalDateTime = LocalDateTime.now(),
    val reportId: String = java.util.UUID.randomUUID().toString()
)

/**
 * DTO para estatísticas diárias dentro do relatório financeiro
 */
data class DailyStatsDto(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDate,
    val totalTransactions: Int,
    val totalAmount: BigDecimal,
    val statusBreakdown: Map<PaymentStatus, Int>,
    val amountBreakdown: Map<PaymentStatus, BigDecimal>
)

/**
 * DTO para critérios de geração de relatório
 */
data class ReportCriteriaDto(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startDate: LocalDate,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    val endDate: LocalDate,
    
    val groupBy: GroupByOption = GroupByOption.DAY,
    val includeStatusBreakdown: Boolean = true,
    val includeRefunds: Boolean = true,
    val format: ReportFormat = ReportFormat.JSON
)

/**
 * Opções de agrupamento para relatórios
 */
enum class GroupByOption {
    DAY,
    WEEK,
    MONTH
}

/**
 * Formatos disponíveis para exportação de relatórios
 */
enum class ReportFormat {
    JSON,
    CSV,
    PDF
} 