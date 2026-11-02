# Payment Service Java

Microsserviço demonstrativo para estudo de desenvolvimento backend com Java e Spring Boot. O projeto implementa uma API REST inicial para registrar e consultar pagamentos em banco H2, com persistência via Spring Data JPA e validação de entrada.

 > **Aviso:** este é um projeto de portfólio, não um sistema de pagamentos pronto para produção. Não movimenta dinheiro nem se integra a adquirentes ou gateways reais. Não envie dados de cartão ou informações financeiras reais.

## Limites de segurança e acesso\n\nA API ainda **não implementa autenticação nem autorização**, nem limita/pagina a listagem de pagamentos. Portanto, todas as entradas de demonstração existentes podem ser listadas por qualquer cliente local que alcançar a API. O servidor está configurado para escutar somente em `127.0.0.1` (`server.address=127.0.0.1`), portanto os endpoints são para uso local e não recebem conexões remotas diretamente. Não remova essa restrição nem exponha o serviço à internet antes de implementar autenticação, autorização, tratamento seguro de erros, configuração de produção e testes de segurança.

## Tecnologias

- Java 17
- Spring Boot
- Maven Wrapper
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- H2 para desenvolvimento e testes
- JUnit 5

## Requisitos

- JDK 17
- O Maven Wrapper incluído no repositório

## Executar localmente

Clone o repositório:

```bash
git clone https://github.com/juniordev830/payment-service-java.git
cd payment-service-java
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
chmod +x mvnw
./mvnw test
./mvnw spring-boot:run
```

Por padrão, a aplicação inicia em `http://localhost:8080`.

## Endpoints disponíveis

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/pagamentos/status` | Verifica se o endpoint está respondendo |
| `GET` | `/api/pagamentos` | Lista pagamentos cadastrados |
| `GET` | `/api/pagamentos/{id}` | Consulta um pagamento por ID |
| `POST` | `/api/pagamentos` | Registra um pagamento com status inicial `PENDENTE` |

### Exemplo de criação

```http
POST /api/pagamentos
Content-Type: application/json
```

```json
{
  "valor": 49.90,
  "moeda": "BRL"
}
```

A API valida que o valor seja maior que zero, tenha no máximo duas casas decimais e que a moeda contenha três letras. O código normaliza a moeda para maiúsculas. O status inicial é definido pelo servidor; o cliente não pode escolher o estado do pagamento no corpo da requisição.

Resposta esperada para uma criação válida: HTTP `201 Created`, com o pagamento persistido, seu ID, status e horário de criação.

## Modelo de dados

Cada registro contém:

- `id`: identificador gerado pelo banco.
- `valor`: valor monetário decimal, sem usar ponto flutuante binário.
- `moeda`: código de três letras, como `BRL`.
- `status`: `PENDENTE`, `APROVADO`, `RECUSADO` ou `CANCELADO`.
- `criadoEm`: data/hora de criação em UTC.

Os estados adicionais são apenas valores do modelo neste estágio; não existe ainda fluxo de aprovação, cancelamento ou integração com um provedor.

## Testes

Execute a suíte com:

```bash
./mvnw test
```

No Windows PowerShell:

```powershell
.\mvnw.cmd test
```

A suíte JUnit 5 executa testes com Spring Boot, MockMvc e repositório JPA. Ela cobre persistência e consulta por ID, HTTP 201/400/404, normalização de moeda, rejeição de valores e moedas inválidos, impossibilidade de definir o status inicial pelo corpo da requisição e erros JSON de validação estruturados.

## Próximas etapas

- Criar uma camada de serviço para separar regras de negócio do controlador.
- Adicionar paginação e ordenação à listagem.
- Expandir a padronização das respostas de erro (a validação e JSON malformado já possuem erros estruturados; recurso não encontrado retorna 404).
- Implementar idempotência e transições de estado com regras explícitas.
- Adicionar autenticação, autorização e trilha de auditoria.
- Migrar de H2 para PostgreSQL em um ambiente de implantação.
- Integrar um provedor de pagamentos em ambiente sandbox, sem armazenar dados sensíveis de cartão.

## Autor

João Junior — estudante de Engenharia de Software, desenvolvendo projetos práticos de backend.

[GitHub: @juniordev830](https://github.com/juniordev830)
