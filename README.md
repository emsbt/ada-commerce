# Ada Commerce - E-Commerce

Este projeto foi desenvolvido para a Ada Tech com o objetivo de criar um sistema de E-Commerce para venda de produtos. O sistema permite o cadastro, listagem e atualização de clientes e produtos, além de realizar vendas seguindo regras de negócio específicas.

## Arquitetura Utilizada

O sistema foi construído com base nos princípios de **Orientação a Objetos (OO)** e **SOLID**, visando modularidade, escalabilidade e fácil manutenção. A estrutura principal é dividida em três camadas:

- **Camada de Apresentação (UI):** Implementada com **JavaFX**, responsável pela interação com o usuário. Utiliza arquivos CSS para customização visual das telas.
- **Camada de Negócios (Service):** Responsável por toda a lógica de negócio, validações e regras descritas no escopo do projeto.
- **Camada de Persistência (Repository):** Inicialmente utiliza um banco de dados em memória. Porém, está preparada para persistir informações em um banco de dados online, garantindo flexibilidade e escalabilidade.

### Diagrama da Arquitetura

```
[JavaFX + CSS] <--> [Service] <--> [Repository/Banco]
      |                |                |
  Interface       Lógica de       Persistência
  Gráfica         Negócio         dos Dados
```

## Funcionalidades

- **Clientes**
  - Cadastro, listagem e atualização de clientes
  - Documento de identificação obrigatório
  - Não é permitido excluir clientes (histórico preservado)

- **Produtos**
  - Cadastro, listagem e atualização de produtos
  - Não é permitido excluir produtos (histórico preservado)

- **Vendas**
  - Adição, remoção e alteração de itens (produtos) em um pedido
  - Registro de quantidade e preço do item no momento da venda
  - Finalização de pedidos (status, pagamento, entrega)
  - Notificações por e-mail para clientes (pagamento, entrega)

## Regras de Negócio

- Todo cliente deve possuir documento de identificação.
- Pedido registra data de criação e inicia com status "Aberto".
- Apenas pedidos "Abertos" podem receber ou alterar itens.
- Valor do produto na venda pode ser diferente do valor do cadastro.
- Pedido só pode ser finalizado se tiver ao menos um item e valor > 0; status alterado para "Aguardando pagamento" e cliente notificado.
- Pagamento apenas para pedidos com status "Aguardando pagamento"; após pagamento, status alterado para "Pago" e cliente notificado.
- Entrega realizada após pagamento, status alterado para "Finalizado" e cliente notificado.

## Interface Gráfica

A interface é construída utilizando **JavaFX** com customização via **CSS**. As telas permitem:

- Cadastro e atualização de clientes e produtos
- Listagem de clientes e produtos
- Realização e acompanhamento de vendas

Exemplo de tela customizada com CSS:

```css
.button {
  -fx-background-color: #2e8b57;
  -fx-text-fill: #fff;
  -fx-font-size: 16px;
  -fx-padding: 10 20 10 20;
}
```

## Persistência dos Dados

A aplicação está preparada para persistir informações em um banco de dados online. Basta configurar as credenciais de acesso no arquivo `application.properties` ou equivalente.

Exemplo de configuração (MySQL):

```properties
db.url=jdbc:mysql://SEU_HOST:3306/ada_commerce
db.username=SEU_USUARIO
db.password=SUA_SENHA
```

A camada `Repository` faz o gerenciamento das operações de CRUD usando JDBC ou um ORM, como Hibernate/JPA.

## Como Executar

1. Clone o repositório:  
   `git clone https://github.com/SEU_USUARIO/ada-commerce.git`

2. Configure o banco de dados, se desejar persistência online.

3. Execute a aplicação JavaFX através da IDE ou pelo terminal:
   `java -jar ada-commerce.jar`

ada-commerce/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── br/
    │   │       └── com/
    │   │           └── adacommerce/
    │   │               ├── application/
    │   │               │   └── Main.java
    │   │               ├── controller/
    │   │               │   ├── ClientesController.java
    │   │               │   ├── LoginController.java
    │   │               │   └── PrincipalController.java
    │   │               ├── database/
    │   │               │   ├── ClienteDAO.java
    │   │               │   ├── DatabaseConnection.java
    │   │               │   ├── PedidoDAO.java
    │   │               │   └── ProdutoDAO.java
    │   │               ├── model/
    │   │               │   ├── Cliente.java
    │   │               │   ├── ItemPedido.java
    │   │               │   ├── Pedido.java
    │   │               │   ├── Produto.java
    │   │               │   └── StatusPedido.java
    │   │               └── service/
    │   │                   └── EmailService.java
    │   └── resources/
    │       ├── css/
    │       │   └── styles.css
    │       └── view/
    │           ├── Clientes.fxml
    │           ├── Login.fxml
    │           └── Principal.fxml
    └── test/
        └── java/
