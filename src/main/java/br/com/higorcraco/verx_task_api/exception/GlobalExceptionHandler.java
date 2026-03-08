package br.com.higorcraco.verx_task_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Falha na validação: method={} path={} details={}", request.getMethod(), request.getRequestURI(), details);
        return build(HttpStatus.BAD_REQUEST, request, "Falha na validação", details);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Parâmetro obrigatório ausente: method={} path={} param={}", request.getMethod(), request.getRequestURI(), ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, request, "Parâmetro obrigatório ausente", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String details = String.format("Parâmetro '%s' com valor inválido: '%s'", ex.getName(), ex.getValue());
        log.warn("Tipo de parâmetro inválido: method={} path={} details={}", request.getMethod(), request.getRequestURI(), details);
        return build(HttpStatus.BAD_REQUEST, request, "Tipo de parâmetro inválido", details);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(
            EmailAlreadyExistsException ex, HttpServletRequest request) {

        log.warn("Conflito de email: method={} path={}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.CONFLICT, request, "Conflito", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Recurso não encontrado: method={} path={} message={}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, request, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedAccessException ex, HttpServletRequest request) {

        log.warn("Acesso não autorizado: method={} path={}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.FORBIDDEN, request, "Acesso negado", ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidTokenException ex, HttpServletRequest request) {

        log.warn("Token inválido: method={} path={}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, request, "Token inválido", ex.getMessage());
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("Falha de autenticação: method={} path={}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, request, "Não autorizado", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Acesso negado: method={} path={}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.FORBIDDEN, request, "Acesso negado", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Erro interno não tratado: method={} path={}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, request, "Erro interno do servidor", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, HttpServletRequest request, String message, String details) {

        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                request.getRequestURI(),
                message,
                details
        ));
    }
}
