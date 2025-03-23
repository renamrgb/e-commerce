package com.ecommerce.order.infrastructure.exception

import com.ecommerce.order.domain.exception.OrderNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.validation.ConstraintViolationException

@ControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @ExceptionHandler(OrderNotFoundException::class)
    fun handleOrderNotFoundException(ex: OrderNotFoundException): ResponseEntity<ErrorResponse> {
        logger.error("Pedido não encontrado: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Pedido não encontrado",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.error("Argumento inválido: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Argumento inválido",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        logger.error("Estado inválido: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "Operação não permitida no estado atual",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }
    
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        logger.error("Acesso negado: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            message = "Acesso negado",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        logger.error("Erro de validação: ${ex.message}")
        
        val errors: Map<String, String> = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as? FieldError)?.field ?: error.objectName
            val errorMessage = error.defaultMessage ?: "Erro de validação"
            fieldName to errorMessage
        }
        
        val errorResponse = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Erro de validação",
            timestamp = System.currentTimeMillis(),
            errors = errors
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ValidationErrorResponse> {
        logger.error("Erro de validação: ${ex.message}")
        
        val errors = ex.constraintViolations.associate { violation ->
            val path = violation.propertyPath.toString()
            val message = violation.message
            path to message
        }
        
        val errorResponse = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Erro de validação",
            timestamp = System.currentTimeMillis(),
            errors = errors
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Erro interno: ${ex.message}", ex)
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "Erro interno no servidor",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    
    data class ErrorResponse(
        val status: Int,
        val message: String,
        val timestamp: Long
    )
    
    data class ValidationErrorResponse(
        val status: Int,
        val message: String,
        val timestamp: Long,
        val errors: Map<String, String>
    )
} 