# Checklist de preparação para implantação — Payment Service Java

> **Estado atual: protótipo local. NÃO implantar como processador de pagamentos reais.**
> Este documento é um plano técnico e não certifica conformidade, segurança ou prontidão de produção.

## Contrato e limitações verificadas na documentação do projeto

- A aplicação usa Java 17, Spring Boot, Spring Data JPA e H2 para desenvolvimento e testes.
- A API está limitada a `127.0.0.1` e só o endpoint `GET /api/pagamentos/status` é público.
- Tokens estáticos separados controlam leitura e escrita; não identificam um usuário ou proprietário.
- A API registra pagamentos demonstrativos com status inicial `PENDENTE`; não realiza liquidação financeira.
- O endpoint de listagem é paginado. Contas distintas com token válido não têm isolamento de registros.
- Não inserir PAN, CVV, senhas, dados bancários ou dados pessoais reais nos exemplos, logs ou banco H2.

## Bloqueadores antes de acesso pela internet

| Área | Risco / lacuna | Critério objetivo de aceite |
| --- | --- | --- |
| Identidade | Token compartilhado identifica serviço, não usuário | Login ou autenticação de serviço com identidades rotacionáveis; matriz de papéis definida |
| Autorização | Leitura transversal de todos os registros | Testes negativos provam que A não lê/edita pagamento de B; consultas filtram por proprietário |
| Transporte | Configuração atual é apenas local | TLS na borda, proxy configurado e revisão de cabeçalhos e CORS |
| Persistência | H2 de desenvolvimento | PostgreSQL com migrações versionadas, backup, restauração testada e permissões mínimas |
| Integridade | Idempotência opcional e global na demo; POST sem chave ainda pode duplicar | Exigir chave por identidade, persistir com migrações, testar concorrência e retenção/replay antes de uso real |
| Segredos | Tokens em variáveis de ambiente | Gerenciador de segredos, rotação, logs sem credenciais e procedimento de revogação |
| Observabilidade | Ausência de plano operacional completo | Logs estruturados sem dados sensíveis, métricas, alertas e IDs de correlação |
| Abuso | Limites de tráfego não documentados | Rate limit por identidade/IP confiável, limites de payload, timeout e testes de abuso |
| Ciclo de estados | Sem transições implementadas | Máquina de estados explícita, permissões e testes de transições proibidas |
| Operação | Sem evidência de restauração/reversão | Pipeline com testes e análise de dependências, deploy reversível e runbook |

## Ordem sugerida de implementação

1. **Especificar a fronteira de confiança:** definir atores, recursos, ambientes, fluxos, ameaça de replay e escopo dos tokens. Modelar permissões antes de ampliar endpoints.
2. **Adicionar identidade e isolamento:** introduzir proprietário em cada registro e validar acesso no serviço e nas queries. Cobrir acessos cruzados com testes MockMvc.
3. **Completar a criação idempotente:** a API demonstrativa já suporta chave única opcional, mas falta associá-la à identidade autenticada, exigi-la em operações reais, adicionar migrações e cobrir chamadas simultâneas e replay de longo prazo.
4. **Migrar persistência com segurança:** criar migrações, índices e estratégias de retenção, executar testes com PostgreSQL real no CI e testar recuperação de backup.
5. **Endurecer a borda:** terminar TLS, configurar proxy confiável, limites de requisição, CORS restrito e política de logs; validar cabeçalhos e erros em testes.
6. **Validar ponta a ponta:** teste de carga controlado, falhas de banco, falha de rede, tentativas de abuso e regressão de autorização. Documentar resultados observados, não presumidos.

## Roteiro de validação local

```bash
./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd test
```

Os comandos acima verificam a suíte existente, **não** os bloqueadores listados aqui. Não confundir o sucesso do CI com aprovação para exposição pública.

## Requisitos para eventual integração financeira

- Usar somente ambiente **sandbox** de provedor reconhecido, sem dados de cartão reais.
- Validar assinaturas e proteção contra replay de webhooks, além de reprocessamento idempotente.
- Delegar captura de dados de cartão ao provedor; avaliar obrigações regulatórias e de PCI DSS com especialistas antes de qualquer operação real.
- Registrar conciliação, auditoria e procedimentos para estornos, falhas e disputas.

## Definição de pronto

A implantação externa só deve ser considerada depois que cada linha do quadro de bloqueadores tiver implementação revisada, testes automatizados e evidência operacional. Até lá, manter bind local e dados sintéticos.
