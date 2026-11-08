package br.com.joao.pagamentos;

import java.math.BigDecimal;

/**
 * Demonstration-only summary of amounts registered in the local database.
 * Includes PENDING and all other statuses; not settled revenue or cash flow.
 */
public record ResumoMoedaResponse(
        String moeda,
        long quantidade,
        BigDecimal valorTotalRegistrado
) {
}
