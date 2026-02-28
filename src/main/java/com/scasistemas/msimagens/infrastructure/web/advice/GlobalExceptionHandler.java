package com.scasistemas.msimagens.infrastructure.web.advice;

import com.scasistemas.msimagens.application.dto.common.ErrorResponse;
import com.scasistemas.msimagens.domain.exceptions.AccountLockedException;
import com.scasistemas.msimagens.domain.exceptions.BusinessException;
import com.scasistemas.msimagens.domain.exceptions.CloudflareException;
import com.scasistemas.msimagens.domain.exceptions.DuplicateUsernameException;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.TokenRevokedException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse.of(
                                                HttpStatus.FORBIDDEN.value(),
                                                "Forbidden",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        /**
         * Captura BadCredentialsException (Spring Security) lançada pelo use case de
         * autenticação, evitando que chegue ao ExceptionTranslationFilter com corpo
         * vazio.
         */
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(
                                                HttpStatus.UNAUTHORIZED.value(),
                                                "Unauthorized",
                                                "Credenciais inválidas",
                                                request.getRequestURI()));
        }

        /**
         * Captura qualquer outra AuthenticationException do Spring Security que possa
         * ser lançada durante o processamento do request.
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(
                                                HttpStatus.UNAUTHORIZED.value(),
                                                "Unauthorized",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(
                                                HttpStatus.NOT_FOUND.value(),
                                                "Not Found",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(
                                                HttpStatus.UNAUTHORIZED.value(),
                                                "Unauthorized",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(TokenRevokedException.class)
        public ResponseEntity<ErrorResponse> handleTokenRevoked(TokenRevokedException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(
                                                HttpStatus.UNAUTHORIZED.value(),
                                                "Unauthorized",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(AccountLockedException.class)
        public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.LOCKED)
                                .body(ErrorResponse.of(
                                                HttpStatus.LOCKED.value(),
                                                "Account Locked",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(DuplicateUsernameException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateUsername(DuplicateUsernameException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(
                                                HttpStatus.CONFLICT.value(),
                                                "Conflict",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(ErrorResponse.of(
                                                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                                                "Unprocessable Entity",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(CloudflareException.class)
        public ResponseEntity<ErrorResponse> handleCloudflare(CloudflareException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.BAD_GATEWAY)
                                .body(ErrorResponse.of(
                                                HttpStatus.BAD_GATEWAY.value(),
                                                "Bad Gateway",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body(ErrorResponse.of(
                                                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                                                "Unsupported Media Type",
                                                "Use multipart/form-data com o campo 'produto' (JSON). Para criar sem imagem, envie apenas o campo 'produto'.",
                                                request.getRequestURI()));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Bad Request",
                                                "Corpo da requisição inválido ou mal formatado.",
                                                request.getRequestURI()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                String message = ex.getBindingResult().getFieldErrors()
                                .stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.joining("; "));

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Bad Request",
                                                message,
                                                request.getRequestURI()));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {
                Class<?> requiredType = ex.getRequiredType();
                String valoresAceitos = "";
                if (requiredType != null && requiredType.isEnum()) {
                        Object[] constants = requiredType.getEnumConstants();
                        valoresAceitos = " Valores aceitos: " + java.util.Arrays.stream(constants)
                                        .map(Object::toString)
                                        .collect(java.util.stream.Collectors.joining(", ")) + ".";
                }
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Bad Request",
                                                "Parâmetro '" + ex.getName() + "' com valor inválido: '" + ex.getValue()
                                                                + "'." + valoresAceitos,
                                                request.getRequestURI()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneral(Exception ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "Internal Server Error",
                                                "Ocorreu um erro interno. Contate o suporte.",
                                                request.getRequestURI()));
        }
}
