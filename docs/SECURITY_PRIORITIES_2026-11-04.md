# Próximas prioridades de segurança — Payment Service Java

**Revisão:** 04/11/2026. **Estado:** plano validado contra `Pagamento.java`, `PagamentoController.java`, `PaymentAccessFilter.java` e `docs/PRODUCTION_READINESS.md`. Este documento não substitui testes/implementação.

## Evidências verificadas

- `Pagamento` contém id, valor, moeda, status e data; **não** possui referência a proprietário/conta/tenant.
- `GET /api/pagamentos`, `GET /api/pagamentos/{id}` e `GET /api/pagamentos/resumo` consultam dados globais, sem filtro de proprietário.
- O filtro exige token de serviço de leitura/administração e impede escrita com token de leitura, mas **não identifica o dono do pagamento**.
- A base é H2 demonstrativa; os tokens são compartilhados, sem revogação/rotação individual. Não há integração real para cobrança ou liquidação.
- A API usa bind local `127.0.0.1` e console H2 desabilitado. Os controles de cabeçalhos, no-store e erros genéricos estão propostos no PR atual; CI verde não constitui autorização para uso financeiro real.

## Ordem confirmada por risco

### P0 — Identidade, autoria e autorização por pagamento (bloqueador)

1. Definir sujeito autenticado: cliente/conta `ownerId` (usuário ou organização) e identidade de serviço com escopos distintos. Tokens compartilhados atuais são apenas solução de laboratório.
2. Incorporar `ownerId` **obrigatório e não editável** no registro e índice por proprietário. Identidade do proprietário deve vir do principal autenticado, nunca de JSON arbitrário enviado pelo cliente.
3. Filtrar consultas de listagem/status/resumo **no banco**, por proprietário. Busca por ID deve confirmar que o pagamento pertence ao principal antes de devolver informações; negar acesso cruzado.
4. Autorizar operação + recurso: `payments:read` para leitura; `payments:create` para criação dentro da conta autorizada. Nenhum papel global deve liberar dados de todos por acidente.
5. Escolher mecanismo padrão de autenticação/verificação (issuer, audience, expiração, assinatura, rotação de chaves) por framework Spring Security ou provedor de identidade confiável. Não confiar apenas em `X-User-Id` do cliente.
6. Migração dos pagamentos já existentes: nada de atribuir registros legados automaticamente ao primeiro usuário; usar plano explícito e dados sintéticos.

**Aceite automatizado obrigatório:** A não lista, consulta por ID ou obtém resumo de B; um usuário A não cria pagamento em nome de B; sem credencial retorna 401, sem escopo retorna 403, recurso fora do escopo responde 404/403 conforme política documentada; tentativas com tokens inválidos/expirados são rejeitadas.

### P0 — Integridade e implantação segura

- Criação de pagamento requer **idempotência** antes de integração com gateway: chave única por operação/conta, replay de mesmo payload não duplica, replay divergente resulta em conflito, chamadas concorrentes são seguras.
- Nunca conectar a contas reais enquanto não houver autorização, idempotência, migrações e configuração de infraestrutura auditadas.
- TLS na borda, proxy confiável, testes de abuso, limitação de taxa e payload, auditoria das ações e logs sem credenciais/dados bancários reais.

### P1 — Gestão de segredos e persistência

- Tokens/chaves em secret manager ou ambiente seguro, rotação com identificador de versão e mecanismo de revogação, logs sem tokens, acesso mínimo.
- Evoluir H2 para PostgreSQL com migrações, backup e restauração comprovados. Preferir testes de integração com banco real.

### P1 — Testes/CI

- Testes MockMvc de `401/403`, token expirado, roles e acesso cruzado em todos os endpoints; não confundir status verde atual com isolamento entre usuários.
- Testes de idempotência paralela e conflitos; teste de replay, falha do banco e auditoria sem PII.
- Conferir qualidade do pipeline e análise de dependências no commit exato antes de PR/merge.

## Sequência mínima de PRs separados

1. **Contrato de identidade e modelo de proprietário**, com especificação de roles e schema.
2. **Persistência/migrations**, índices e isolamento das queries; testes negativos de A x B.
3. **Autenticação Spring Security e autorização**, retirando tokens compartilhados quando houver migração segura.
4. **Idempotência e testes concorrentes** antes de qualquer fluxo financeiro externo.
5. **Readiness de implantação**: TLS, gestão de segredos, logs, bancos e rollback verificados.

**Critério de liberação:** uso externo com pagamentos reais permanece bloqueado até completar P0 e as verificações necessárias. O documento não altera `main`.
