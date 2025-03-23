package com.ecommerce.catalog.infrastructure.exception

import com.ecommerce.catalog.domain.exception.EntityNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: WebRequest): ResponseEntity<ApiError> {
        log.error("Erro inesperado: ", ex)
        val apiError = ApiError(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Erro interno do servidor",
            message = "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity(apiError, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: EntityNotFoundException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Recurso não encontrado",
            message = ex.message ?: "O recurso solicitado não foi encontrado",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity(apiError, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Requisição inválida",
            message = ex.message ?: "A requisição contém parâmetros inválidos",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Operação não permitida",
            message = ex.message ?: "A operação solicitada não pode ser realizada no estado atual",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity(apiError, HttpStatus.CONFLICT)
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errors = ex.bindingResult.fieldErrors.associate { fieldError: FieldError ->
            fieldError.field to (fieldError.defaultMessage ?: "Erro de validação")
        }

        val apiError = ValidationApiError(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Erro de validação",
            message = "A requisição contém campos inválidos",
            errors = errors,
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }
}

data class ApiError(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

data class ValidationApiError(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val errors: Map<String, String>,
    val path: String
) 