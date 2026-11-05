# Payment Service Java

Microsserviço demonstrativo para estudo de desenvolvimento backend com Java e Spring Boot. O projeto implementa uma API REST inicial para registrar e consultar pagamentos em banco H2, com persistência via Spring Data JPA e validação de entrada.

 > **Aviso:** este é um projeto de portfólio, não um sistema de pagamentos pronto para produção. Não movimenta dinheiro nem se integra a adquirentes ou gateways reais. Não envie dados de cartão ou informações financeiras reais.

## Limites de segurança e acesso

A API exige tokens de serviço distintos para leitura e administração; veja [configuração de acesso](#acesso-aos-pagamentos). Apenas `GET /api/pagamentos/status` é público. O filtro exige autenticação por padrão em todas as demais requisições, inclusive rotas desconhecidas e variações de URL, evitando depender da interpretação do caminho bruto como fronteira de segurança. Ele também funciona quando a aplicação utiliza um context path.

A listagem é paginada (padrão: 20; máximo: 50 registros). Os tokens não representam usuários e não isolam pagamentos por proprietário: qualquer portador de um token de leitura ou administração pode consultar todos os registros demonstrativos. O serviço continua limitado a `127.0.0.1`. Antes de qualquer exposição remota, faltam identidade por usuário, autorização por recurso, auditoria, TLS e configuração de produção.

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
| `GET` | `/api/pagamentos?page=0&size=20` | Lista pagamentos por página (página a partir de 0; tamanho de 1 a 50; máximo de 100.000 páginas) |
| `GET` | `/api/pagamentos?status=PENDENTE&page=0&size=20` | Filtra por status e mantém a mesma paginação e ordenação |
| `GET` | `/api/pagamentos/{id}` | Consulta um pagamento por ID |
| `POST` | `/api/pagamentos` | Registra um pagamento com status inicial `PENDENTE` |

O parâmetro opcional `status` aceita `PENDENTE`, `APROVADO`, `RECUSADO` ou `CANCELADO`, com letras maiúsculas. Um valor desconhecido retorna HTTP `400 Bad Request`. O filtro usa a consulta paginada no banco; sem `status`, o comportamento original permanece igual. Como o MVP só cria pagamentos pendentes e não implementa transições de estado, os demais status normalmente retornam uma lista vazia.

### Criação idempotente (opt-in no protótipo)

Ao enviar `Idempotency-Key` na criação, o serviço salva um identificador **opaco e não secreto** de 16 a 64 caracteres ASCII (`A-Z`, `a-z`, `0-9`, `_` e `-`). Uma repetição com a mesma chave, valor e moeda normalizada retorna o pagamento original (HTTP 200) em vez de inserir outro. O servidor aceita **somente um cabeçalho `Idempotency-Key`** e rejeita duplicados ou valores combinados por vírgula com HTTP 400. Reutilizar a mesma chave com outro valor ou moeda retorna HTTP 409. Uma chave inválida retorna HTTP 400.

```http
POST /api/pagamentos
Authorization: Bearer <PAYMENT_ADMIN_TOKEN>
Idempotency-Key: example-payment-request-0001
Content-Type: application/json
```

O banco aplica uma restrição **única** à chave para impedir que duas solicitações simultâneas criem registros duplicados com a mesma chave. Para preservar clientes demonstrativos existentes, o cabeçalho ainda é **opcional**: solicitações sem chave continuam criando registros novos a cada chamada. A unicidade é global nesta instalação local, pois o projeto ainda não tem identidade/proprietário por pagamento. Não use dados pessoais, credenciais ou valores secretos nessa chave.

Esta proteção é apenas um **primeiro passo** e não certifica um processador financeiro de produção: faltam identidade e isolamento por proprietário, persistência com migrações, conciliação e políticas de reprocessamento.

### Exemplo de criação

```http
POST /api/pagamentos
Authorization: Bearer <PAYMENT_ADMIN_TOKEN>
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

A suíte JUnit 5 executa testes com Spring Boot, MockMvc e repositório JPA. Ela cobre persistência e consulta por ID, HTTP 201/400/404, normalização de moeda, rejeição de valores e moedas inválidos, impossibilidade de definir o status inicial pelo corpo da requisição e erros JSON de validação estruturados. Os testes de acesso cobrem tokens ausentes/inválidos, permissões de leitura, configuração inválida, context path e variações de URI com codificação e parâmetros de matriz.

## Próximas etapas

- Criar uma camada de serviço para separar regras de negócio do controlador.
- Adicionar autenticação por usuário e regras de acesso aos pagamentos antes de qualquer implantação remota.
- Expandir a padronização das respostas de erro (a validação e JSON malformado já possuem erros estruturados; recurso não encontrado retorna 404).
- Evoluir a idempotência opcional para chaves escopadas por identidade autenticada e política de retenção; implementar transições de estado explícitas.
- Evoluir os tokens de serviço para identidades individuais, autorização por proprietário e trilha de auditoria.
- Migrar de H2 para PostgreSQL em um ambiente de implantação.
- Integrar um provedor de pagamentos em ambiente sandbox, sem armazenar dados sensíveis de cartão.

## Autor

João Junior — estudante de Engenharia de Software, desenvolvendo projetos práticos de backend.

[GitHub: @juniordev830](https://github.com/juniordev830)


## Acesso aos pagamentos

Configure dois tokens diferentes de pelo menos 32 bytes antes de consultar ou criar pagamentos:

```bash
export PAYMENT_READ_TOKEN="substitua-por-um-token-aleatorio-com-32-bytes-ou-mais"
export PAYMENT_ADMIN_TOKEN="use-outro-token-aleatorio-com-32-bytes-ou-mais"
```

Envie `Authorization: Bearer <token>`. O token de leitura autoriza GET e HEAD; o administrativo permite as operações implementadas, incluindo POST. Sem configuração válida, a API responde 503; credenciais ausentes ou inválidas recebem 401, e escrita com token de leitura recebe 403. `GET /api/pagamentos/status` é público. O console H2 fica desabilitado. Este protótipo continua restrito ao loopback e não deve ser publicado como serviço de pagamentos de produção sem identidade, auditoria, armazenamento e TLS adequados.
