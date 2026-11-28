# Engenharia reversa e estudo — Payment Service Java

## Objetivo e fluxo

Registro e consulta de pagamentos de demonstração. Siga o DTO de criação até o controlador, o repositório e a entidade Pagamento. A configuração do Spring e o pom.xml documentam a camada de persistência.

**Tecnologias identificadas** · Java.

**Revisão de referência** · `85f024effa66`.

## Mapa do código

| Caminho | Papel de leitura | Símbolos selecionados |
| --- | --- | --- |
| [src/main/java/br/com/joao/pagamentos/ApiExceptionHandler.java](../src/main/java/br/com/joao/pagamentos/ApiExceptionHandler.java) | Implementação | `ApiExceptionHandler`, `tratarCorpoInvalido`, `tratarValidacao` |
| [src/main/java/br/com/joao/pagamentos/CriarPagamentoRequest.java](../src/main/java/br/com/joao/pagamentos/CriarPagamentoRequest.java) | Implementação | `CriarPagamentoRequest` |
| [src/main/java/br/com/joao/pagamentos/MicrosservicoPagamentosApplication.java](../src/main/java/br/com/joao/pagamentos/MicrosservicoPagamentosApplication.java) | Implementação | `MicrosservicoPagamentosApplication`, `main` |
| [src/main/java/br/com/joao/pagamentos/Pagamento.java](../src/main/java/br/com/joao/pagamentos/Pagamento.java) | Implementação | `Pagamento`, `prepararPersistencia`, `getId`, `getValor`, `getMoeda`, `getStatus`, `getCriadoEm` |
| [src/main/java/br/com/joao/pagamentos/PagamentoController.java](../src/main/java/br/com/joao/pagamentos/PagamentoController.java) | Implementação | `PagamentoController`, `StatusRequest`, `listar`, `buscarPorId`, `criar`, `registrarStatus`, `resumo`, `resumoPorMoeda`, `status` |
| [src/main/java/br/com/joao/pagamentos/PagamentoRepository.java](../src/main/java/br/com/joao/pagamentos/PagamentoRepository.java) | Implementação | `PagamentoRepository` |
| [src/main/java/br/com/joao/pagamentos/PaymentAccessFilter.java](../src/main/java/br/com/joao/pagamentos/PaymentAccessFilter.java) | Implementação | `PaymentAccessFilter`, `doFilterInternal` |
| [src/main/java/br/com/joao/pagamentos/ResumoMoedaResponse.java](../src/main/java/br/com/joao/pagamentos/ResumoMoedaResponse.java) | Implementação | `ResumoMoedaResponse` |
| [src/main/java/br/com/joao/pagamentos/StatusPagamento.java](../src/main/java/br/com/joao/pagamentos/StatusPagamento.java) | Implementação | `StatusPagamento` |

## Configuração e referências

Ambiente e dependências: [pom.xml](../pom.xml).

Referências existentes: [docs/GUIA_DE_VALIDACAO.md](GUIA_DE_VALIDACAO.md).

Consulte o [contrato HTTP](API.md) para os parâmetros e o significado das respostas.

## Roteiro de estudo e desenvolvimento

1. Comece pelo fluxo descrito acima e localize seus pontos de entrada no mapa. Acompanhe uma entrada válida até a transformação ou a resposta.
2. Abra um teste relacionado ao fluxo e descreva qual regra ele protege. Compare o tratamento esperado para entradas válidas, inválidas e nos limites.
3. Ao adicionar uma regra, determine qual módulo controla o dado e cubra o caso nesse nível. Em seguida, confira o efeito no comando, na API ou na interface de entrada.

**Pergunta de entrevista**

Como você preservaria os valores monetários ao adicionar uma nova moeda?

## Testes versionados

Arquivos para começar a leitura dos cenários de teste: [src/test/java/br/com/joao/pagamentos/DesfechoPagamentoTests.java](../src/test/java/br/com/joao/pagamentos/DesfechoPagamentoTests.java), [src/test/java/br/com/joao/pagamentos/MicrosservicoPagamentosApplicationTests.java](../src/test/java/br/com/joao/pagamentos/MicrosservicoPagamentosApplicationTests.java), [src/test/java/br/com/joao/pagamentos/PagamentoStatusFilterTests.java](../src/test/java/br/com/joao/pagamentos/PagamentoStatusFilterTests.java), [src/test/java/br/com/joao/pagamentos/PaymentAccessFilterTests.java](../src/test/java/br/com/joao/pagamentos/PaymentAccessFilterTests.java), [src/test/java/br/com/joao/pagamentos/PaymentAccessRouteTests.java](../src/test/java/br/com/joao/pagamentos/PaymentAccessRouteTests.java), [src/test/java/br/com/joao/pagamentos/ResumoMoedaTests.java](../src/test/java/br/com/joao/pagamentos/ResumoMoedaTests.java).

Os comandos e versões de runtime usados no CI ficam em [.github/workflows/ci.yml](../.github/workflows/ci.yml).

[Voltar ao README](../README.md).
