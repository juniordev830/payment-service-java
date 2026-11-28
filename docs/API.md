# API — Siga o DTO de criação até o controlador, o repositório e a entidade Pagamento. A configuração do Spring e o pom.xml documentam a camada de persistência.

O Spring Boot abre a API local no loopback por padrão. O controlador encaminha as leituras e gravações ao repositório JPA. A configuração e eventuais dependências estão no README e no pom.xml.

| Recurso | Comportamento |
| --- | --- |
| `GET /api/pagamentos` | Lista registros por data e id decrescentes. Aceita status, page (padrão 0) e size (padrão 20, máximo 50). |
| `GET /api/pagamentos/{id}` | Retorna um pagamento. Id ausente no banco recebe 404. |
| `POST /api/pagamentos` | Registra valor de pelo menos 0,01, até dez dígitos inteiros e duas casas decimais, com código de moeda de três letras. Retorna 201. |
| `PUT /api/pagamentos/{id}/status` | Registra um desfecho APROVADO, RECUSADO ou CANCELADO. PENDENTE recebe 400; transição conflitante recebe 409. |
| `GET /api/pagamentos/resumo` | Conta registros por status e no total. |
| `GET /api/pagamentos/resumo/moeda` | Recebe moeda e soma os pagamentos registrados nessa moeda, incluindo pendentes. |
| `GET /api/pagamentos/status` | Retorna a sinalização simples de disponibilidade. |

```bash
curl 'http://127.0.0.1:8080/api/pagamentos?status=PENDENTE&size=10'
```

O valor é representado como BigDecimal. O resumo por moeda é uma soma de registros e inclui valores pendentes.

Implementação em [src/main/java/br/com/joao/pagamentos/PagamentoController.java](../src/main/java/br/com/joao/pagamentos/PagamentoController.java). [Guia do código](ENGENHARIA_REVERSA.md).
