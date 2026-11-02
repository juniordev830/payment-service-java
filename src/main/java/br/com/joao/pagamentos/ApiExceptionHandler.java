package br.com.joao.pagamentos;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Produces a stable, client-safe response for request validation failures.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarCorpoInvalido(
            HttpMessageNotReadableException exception) {
        Map<String, String> erros = new LinkedHashMap<>();
        erros.put("body", "JSON inválido ou incompleto");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("message", "O corpo da requisição é inválido");
        corpo.put("errors", erros);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(
            MethodArgumentNotValidException exception) {
        Map<String, String> erros = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(erro ->
                erros.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("message", "A requisição contém campos inválidos");
        corpo.put("errors", erros);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }
}
