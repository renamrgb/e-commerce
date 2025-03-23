package com.ecommerce.payment.application.service

import com.ecommerce.payment.application.dto.*
import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.domain.model.PaymentStatus
import com.ecommerce.payment.domain.repository.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.*
import java.util.stream.Collectors

/**
 * Serviço responsável pela geração de relatórios financeiros
 */
@Service
class FinancialReportService(
    private val paymentRepository: PaymentRepository
) {
    private val logger = LoggerFactory.getLogger(FinancialReportService::class.java)
    
    /**
     * Gera um relatório financeiro completo baseado nos critérios fornecidos
     */
    fun generateReport(criteria: ReportCriteriaDto): FinancialReportDto {
        logger.info("Gerando relatório financeiro de {} até {}", criteria.startDate, criteria.endDate)
        
        val startDateTime = LocalDateTime.of(criteria.startDate, LocalTime.MIN)
        val endDateTime = LocalDateTime.of(criteria.endDate, LocalTime.MAX)
        
        // Busca todos os pagamentos no período
        val payments = paymentRepository.findByCreatedAtBetween(startDateTime, endDateTime)
        
        if (payments.isEmpty()) {
            logger.info("Nenhum pagamento encontrado no período solicitado")
            return createEmptyReport(criteria.startDate, criteria.endDate)
        }
        
        // Agrupamento dos pagamentos por status
        val paymentsByStatus = payments.groupBy { it.status }
        
        // Cálculo dos totais
        val totalTransactions = payments.size
        val totalAmount = sumAmounts(payments)
        val avgOrderValue = if (totalTransactions > 0) 
            totalAmount.divide(BigDecimal(totalTransactions), 2, RoundingMode.HALF_UP) 
        else BigDecimal.ZERO
        
        // Cálculo por status
        val completedTransactions = countByStatus(paymentsByStatus, PaymentStatus.COMPLETED)
        val completedAmount = sumByStatus(paymentsByStatus, PaymentStatus.COMPLETED)
        
        val failedTransactions = countByStatus(paymentsByStatus, PaymentStatus.FAILED)
        val failedAmount = sumByStatus(paymentsByStatus, PaymentStatus.FAILED)
        
        val pendingTransactions = countByStatus(paymentsByStatus, PaymentStatus.PENDING)
        val pendingAmount = sumByStatus(paymentsByStatus, PaymentStatus.PENDING)
        
        val refundedTransactions = countByStatus(paymentsByStatus, PaymentStatus.REFUNDED)
        val refundedAmount = sumByStatus(paymentsByStatus, PaymentStatus.REFUNDED)
        
        val canceledTransactions = countByStatus(paymentsByStatus, PaymentStatus.CANCELED)
        val canceledAmount = sumByStatus(paymentsByStatus, PaymentStatus.CANCELED)
        
        // Taxa de sucesso
        val successRate = if (totalTransactions > 0) 
            completedTransactions.toDouble() / totalTransactions.toDouble() * 100 
        else 0.0
        
        // Estatísticas diárias
        val dailyStats = generateDailyStats(payments, criteria.startDate, criteria.endDate, criteria.groupBy)
        
        return FinancialReportDto(
            startDate = criteria.startDate,
            endDate = criteria.endDate,
            totalTransactions = totalTransactions,
            totalAmount = totalAmount,
            avgOrderValue = avgOrderValue,
            completedTransactions = completedTransactions,
            completedAmount = completedAmount,
            failedTransactions = failedTransactions,
            failedAmount = failedAmount,
            pendingTransactions = pendingTransactions,
            pendingAmount = pendingAmount,
            refundedTransactions = refundedTransactions,
            refundedAmount = refundedAmount,
            canceledTransactions = canceledTransactions,
            canceledAmount = canceledAmount,
            successRate = successRate,
            dailyStats = dailyStats
        )
    }
    
    /**
     * Exporta um relatório financeiro no formato especificado
     */
    fun exportReport(report: FinancialReportDto, format: ReportFormat): ByteArray {
        return when (format) {
            ReportFormat.CSV -> exportToCsv(report)
            ReportFormat.PDF -> exportToPdf(report)
            else -> throw UnsupportedOperationException("Formato de relatório não suportado: $format")
        }
    }
    
    /**
     * Gera estatísticas por dia, semana ou mês
     */
    private fun generateDailyStats(
        payments: List<Payment>, 
        startDate: LocalDate, 
        endDate: LocalDate,
        groupBy: GroupByOption
    ): List<DailyStatsDto> {
        val result = mutableListOf<DailyStatsDto>()
        val groupedPayments = when (groupBy) {
            GroupByOption.DAY -> groupPaymentsByDay(payments)
            GroupByOption.WEEK -> groupPaymentsByWeek(payments)
            GroupByOption.MONTH -> groupPaymentsByMonth(payments)
        }
        
        groupedPayments.forEach { (date, datePayments) ->
            val paymentsByStatus = datePayments.groupBy { it.status }
            
            val statusBreakdown = PaymentStatus.values().associateWith { status ->
                paymentsByStatus[status]?.size ?: 0
            }
            
            val amountBreakdown = PaymentStatus.values().associateWith { status ->
                sumByStatus(paymentsByStatus, status)
            }
            
            result.add(
                DailyStatsDto(
                    date = date,
                    totalTransactions = datePayments.size,
                    totalAmount = sumAmounts(datePayments),
                    statusBreakdown = statusBreakdown,
                    amountBreakdown = amountBreakdown
                )
            )
        }
        
        return result.sortedBy { it.date }
    }
    
    /**
     * Agrupa pagamentos por dia
     */
    private fun groupPaymentsByDay(payments: List<Payment>): Map<LocalDate, List<Payment>> {
        return payments.groupBy { it.createdAt.toLocalDate() }
    }
    
    /**
     * Agrupa pagamentos por semana
     */
    private fun groupPaymentsByWeek(payments: List<Payment>): Map<LocalDate, List<Payment>> {
        val weekFields = WeekFields.of(Locale.getDefault())
        val groupedByWeek = payments.groupBy { 
            val date = it.createdAt.toLocalDate()
            val weekOfYear = date.get(weekFields.weekOfWeekBasedYear())
            val year = date.get(weekFields.weekBasedYear())
            
            // Usar o primeiro dia da semana como chave
            date.with(weekFields.dayOfWeek(), 1)
        }
        
        return groupedByWeek
    }
    
    /**
     * Agrupa pagamentos por mês
     */
    private fun groupPaymentsByMonth(payments: List<Payment>): Map<LocalDate, List<Payment>> {
        return payments.groupBy { 
            val date = it.createdAt.toLocalDate()
            LocalDate.of(date.year, date.month, 1)
        }
    }
    
    /**
     * Conta pagamentos por status
     */
    private fun countByStatus(paymentsByStatus: Map<PaymentStatus, List<Payment>>, status: PaymentStatus): Int {
        return paymentsByStatus[status]?.size ?: 0
    }
    
    /**
     * Soma valores por status
     */
    private fun sumByStatus(paymentsByStatus: Map<PaymentStatus, List<Payment>>, status: PaymentStatus): BigDecimal {
        return paymentsByStatus[status]?.let { sumAmounts(it) } ?: BigDecimal.ZERO
    }
    
    /**
     * Soma valores de uma lista de pagamentos
     */
    private fun sumAmounts(payments: List<Payment>): BigDecimal {
        return payments.fold(BigDecimal.ZERO) { acc, payment -> 
            acc.add(payment.amount) 
        }
    }
    
    /**
     * Cria um relatório vazio quando não há dados
     */
    private fun createEmptyReport(startDate: LocalDate, endDate: LocalDate): FinancialReportDto {
        return FinancialReportDto(
            startDate = startDate,
            endDate = endDate,
            totalTransactions = 0,
            totalAmount = BigDecimal.ZERO,
            avgOrderValue = BigDecimal.ZERO,
            completedTransactions = 0,
            completedAmount = BigDecimal.ZERO,
            failedTransactions = 0,
            failedAmount = BigDecimal.ZERO,
            pendingTransactions = 0,
            pendingAmount = BigDecimal.ZERO,
            refundedTransactions = 0,
            refundedAmount = BigDecimal.ZERO,
            canceledTransactions = 0,
            canceledAmount = BigDecimal.ZERO,
            successRate = 0.0,
            dailyStats = emptyList()
        )
    }
    
    /**
     * Exporta relatório para CSV
     */
    private fun exportToCsv(report: FinancialReportDto): ByteArray {
        logger.info("Exportando relatório para CSV: {}", report.reportId)
        
        val header = "Data,Total de Transações,Valor Total,Completadas,Valor Completadas,Falhas,Valor Falhas,Pendentes,Valor Pendentes,Reembolsadas,Valor Reembolsadas,Canceladas,Valor Canceladas\n"
        
        val dailyRows = report.dailyStats.joinToString("\n") { daily ->
            "${daily.date}," +
            "${daily.totalTransactions}," +
            "${daily.totalAmount}," +
            "${daily.statusBreakdown[PaymentStatus.COMPLETED] ?: 0}," +
            "${daily.amountBreakdown[PaymentStatus.COMPLETED] ?: BigDecimal.ZERO}," +
            "${daily.statusBreakdown[PaymentStatus.FAILED] ?: 0}," +
            "${daily.amountBreakdown[PaymentStatus.FAILED] ?: BigDecimal.ZERO}," +
            "${daily.statusBreakdown[PaymentStatus.PENDING] ?: 0}," +
            "${daily.amountBreakdown[PaymentStatus.PENDING] ?: BigDecimal.ZERO}," +
            "${daily.statusBreakdown[PaymentStatus.REFUNDED] ?: 0}," +
            "${daily.amountBreakdown[PaymentStatus.REFUNDED] ?: BigDecimal.ZERO}," +
            "${daily.statusBreakdown[PaymentStatus.CANCELED] ?: 0}," +
            "${daily.amountBreakdown[PaymentStatus.CANCELED] ?: BigDecimal.ZERO}"
        }
        
        val summary = "\n\nResumo do Período: ${report.startDate} até ${report.endDate}\n" +
                "Total de Transações: ${report.totalTransactions}\n" +
                "Valor Total: ${report.totalAmount}\n" +
                "Ticket Médio: ${report.avgOrderValue}\n" +
                "Taxa de Sucesso: ${report.successRate}%\n"
        
        val content = header + dailyRows + summary
        return content.toByteArray()
    }
    
    /**
     * Exporta relatório para PDF (simulado)
     * Em uma implementação real, usaria uma biblioteca como iText ou Apache PDFBox
     */
    private fun exportToPdf(report: FinancialReportDto): ByteArray {
        logger.info("Exportando relatório para PDF: {}", report.reportId)
        
        // Aqui seria implementada a geração de PDF
        // Por exemplo, usando iText ou Apache PDFBox
        
        // Por enquanto, retornamos uma simulação
        return "Relatório em PDF não implementado. Utilize formato CSV.".toByteArray()
    }
} 