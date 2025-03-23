package com.ecommerce.payment.infrastructure.controller

import com.ecommerce.payment.application.dto.FinancialReportDto
import com.ecommerce.payment.application.dto.ReportCriteriaDto
import com.ecommerce.payment.application.dto.ReportFormat
import com.ecommerce.payment.application.service.FinancialReportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.validation.Valid

/**
 * Controlador para geração e exportação de relatórios financeiros
 */
@RestController
@RequestMapping("/api/v1/admin/reports")
@Tag(name = "Relatórios", description = "Endpoints para geração e exportação de relatórios financeiros")
@PreAuthorize("hasRole('ADMIN')")
@Validated
class ReportController(
    private val financialReportService: FinancialReportService
) {

    private val logger = LoggerFactory.getLogger(ReportController::class.java)
    
    /**
     * Gera um relatório financeiro baseado nos critérios
     */
    @PostMapping("/financial")
    @Operation(
        summary = "Gera relatório financeiro",
        description = "Gera um relatório financeiro completo baseado nos critérios fornecidos"
    )
    fun generateFinancialReport(
        @Valid @RequestBody criteria: ReportCriteriaDto
    ): ResponseEntity<FinancialReportDto> {
        logger.info("Solicitação de relatório financeiro de {} até {}", 
            criteria.startDate, criteria.endDate)
        
        val report = financialReportService.generateReport(criteria)
        
        return ResponseEntity.ok(report)
    }
    
    /**
     * Exporta um relatório financeiro no formato especificado
     */
    @GetMapping("/financial/export")
    @Operation(
        summary = "Exporta relatório financeiro",
        description = "Exporta um relatório financeiro no formato especificado (CSV, PDF)"
    )
    fun exportFinancialReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam(defaultValue = "CSV") format: ReportFormat
    ): ResponseEntity<ByteArray> {
        logger.info("Solicitação de exportação de relatório de {} até {} no formato {}", 
            startDate, endDate, format)
        
        val criteria = ReportCriteriaDto(
            startDate = startDate, 
            endDate = endDate, 
            format = format
        )
        
        val report = financialReportService.generateReport(criteria)
        val reportBytes = financialReportService.exportReport(report, format)
        
        val headers = HttpHeaders()
        val filename = "relatorio-financeiro-${startDate.format(DateTimeFormatter.ISO_DATE)}-${endDate.format(DateTimeFormatter.ISO_DATE)}"
        
        when (format) {
            ReportFormat.CSV -> {
                headers.contentType = MediaType.parseMediaType("text/csv")
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename.csv")
            }
            ReportFormat.PDF -> {
                headers.contentType = MediaType.APPLICATION_PDF
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename.pdf")
            }
            else -> {
                headers.contentType = MediaType.APPLICATION_JSON
            }
        }
        
        return ResponseEntity(reportBytes, headers, HttpStatus.OK)
    }
    
    /**
     * Gera um relatório resumido para o dashboard
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "Relatório para dashboard",
        description = "Gera um relatório resumido para exibição no dashboard administrativo"
    )
    fun generateDashboardReport(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        startDate: LocalDate = LocalDate.now().minusDays(30),
        
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        endDate: LocalDate = LocalDate.now()
    ): ResponseEntity<FinancialReportDto> {
        logger.info("Solicitação de relatório para dashboard de {} até {}", startDate, endDate)
        
        val criteria = ReportCriteriaDto(startDate = startDate, endDate = endDate)
        val report = financialReportService.generateReport(criteria)
        
        return ResponseEntity.ok(report)
    }
} 