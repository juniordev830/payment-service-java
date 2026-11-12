# Guia de manutenção — Payment Service Java

## Escopo confirmado

API de pagamentos demonstrativos em Spring Boot e H2; não processa transações bancárias reais.

## Arquivos principais

`pom.xml`, `PagamentoController.java`, `PaymentAccessFilter.java`, `PagamentoRepository.java` e testes em `src/test/java/`.

## Validar uma alteração

No diretório raiz, usar os comandos adequados ao ambiente: `./mvnw test` no Linux/macOS ou `mvnw.cmd test` no Windows.

Registrar o resultado dos testes no PR e conferir se a execução do GitHub Actions corresponde ao commit revisado. Não presumir que a interface ou o serviço é de produção apenas porque o build passou.

## Revisão de casos críticos

Conferir validação de valores e moedas, filtro por status, concorrência, rejeição de acesso sem token e impossibilidade de usar token de leitura para escrita.

## Limites e próxima entrega

Não testar com cartões ou chaves de pagamento reais. Próxima etapa: separar domínio de transporte, auditoria de mudanças de status e testes de idempotência.

### Checklist para PR

- [ ] A mudança está associada a um comportamento identificável.
- [ ] Foram documentados os casos novos e os limites encontrados.
- [ ] Há teste automatizado quando o módulo comporta execução reproduzível.
- [ ] Exemplos não contêm credenciais ou dados reais.
- [ ] A branch pode ser revisada antes de qualquer merge na principal.
