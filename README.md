# Payment Service Java

Projeto de portfólio em Java 17 com Spring Boot para estudar a estrutura de um serviço relacionado a pagamentos. O repositório inclui configuração Maven, persistência com Spring Data JPA, validação e banco H2 para desenvolvimento/testes.

> Este projeto é demonstrativo. Não processe pagamentos reais nem armazene dados de cartão sem uma arquitetura, provedor e revisão de segurança apropriados.

## Tecnologias

- Java 17
- Spring Boot
- Maven Wrapper
- Spring Data JPA
- Bean Validation
- H2 para desenvolvimento/testes

## Requisitos

- JDK 17
- O Maven Wrapper incluído no repositório

## Compilar e testar

Linux/macOS:

```bash
./mvnw test
./mvnw package
```

Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Para iniciar a aplicação, após conferir as configurações e o ponto de entrada em `src/`:

```bash
./mvnw spring-boot:run
```

## Organização

- `pom.xml`: dependências, versão Java e plugins.
- `mvnw` / `mvnw.cmd`: wrapper para executar Maven sem exigir instalação global.
- `src/main`: implementação da aplicação.
- `src/test`: testes.

## Segurança e limites

- Use dados fictícios em testes.
- Nunca coloque segredos em arquivos versionados.
- Valide valores monetários, moeda, idempotência e transições de estado antes de evoluir o projeto.
- Um sistema de pagamento real precisa de autenticação, autorização, auditoria, tratamento de falhas e integração segura com um provedor de pagamentos.

## Próximas melhorias

- Documentar os endpoints efetivamente implementados.
- Adicionar testes de integração e validação de regras de domínio.
- Definir uma estratégia explícita para idempotência e estados de pagamento.

Projeto em desenvolvimento.
