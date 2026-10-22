package cl.duoc.pedidos360.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;

@RestControllerAdvice
public class PedidoExceptionHandler {

    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<Map<String, String>> manejarSolicitudInvalida(Exception exception) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "pedidoId o clienteEmail tiene un formato inválido."));
    }
}
