package io.github.ferrazsergio.libraryapi.interfaces.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Captura exceções de credenciais inválidas (login/senha errados)
     * e retorna um status 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Tentativa de autenticação inválida: {}", ex.getMessage());
        return Map.of("error", "Invalid credentials", "message", ex.getMessage());
    }

    /**
     * Captura exceções de acesso negado por falta de permissão (roles/authorities)
     * e retorna um status 403 Forbidden.
     */
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleAccessDeniedException(Exception ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return Map.of("error", "Access denied", "message", ex.getMessage());
    }

    /**
     * Captura erros de validação de DTOs (anotados com @Valid)
     * e retorna um status 400 Bad Request com os detalhes dos campos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Erro de validação: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    /**
     * Captura qualquer outra exceção não tratada e retorna um status
     * 500 Internal Server Error para proteger a aplicação.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGenericException(Exception ex) {
        log.error("Erro inesperado no servidor", ex);
        return Map.of("error", "An unexpected server error occurred");
    }
}