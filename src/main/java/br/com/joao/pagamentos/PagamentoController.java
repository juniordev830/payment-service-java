package br.com.joao.pagamentos;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    @GetMapping
    public String listar() {
        return "API de pagamentos funcionando!";
    }
}