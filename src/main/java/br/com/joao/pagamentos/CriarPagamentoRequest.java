package br.com.joao.pagamentos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record CriarPagamentoRequest(
        @NotNull
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais")
        BigDecimal valor,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "A moeda deve conter um código de 3 letras, como BRL")
        String moeda
) {
}
