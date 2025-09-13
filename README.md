******# Ada Commerce - E-Commerce

Visão Geral

O Ada Commerce é um sistema completo de gerenciamento de e-commerce desenvolvido em Java com interface gráfica JavaFX, arquitetura em camadas e banco de dados SQLite.
Como Executar com Maven
Pré-requisitos
Java JDK 11 ou superior

Maven 3.6 ou superior

Comandos para execução
Compilar o projeto:

mvn compile
mvn test

Estrutura do Projeto:

br.com.adacommerce/
├── config/              # Configuração do banco
├── controller/          # Controladores JavaFX (13 controllers)
├── dao/                 # Data Access Objects (2 DAOs)
├── domain/              # Domínios de negócio
│   ├── cliente/         # Domínio de clientes
│   ├── pedido/          # Domínio de pedidos
│   └── produto/         # Domínio de produtos
├── model/               # Modelos de dados
├── report/              # Sistema de relatórios
├── service/             # Serviços de negócio (7 services)
├── session/             # Gerenciamento de sessão
├── util/                # Utilitários
└── resources/           # Recursos da aplicação
├── css/             # Estilos CSS
├── fxml/            # Views JavaFX (13 telas)
└── configurações    # Configurações adicionais

Configuração do Banco de Dados

DatabaseConfig.java

Sistema robusto de inicialização e migração do banco SQLite:

DatabaseConfig.initialize();
Connection conn = DatabaseConfig.getConnection();

Funcionalidades do Banco:

Tabelas Criadas Automaticamente:
usuario - Sistema de autenticação com usuário admin padrão

categoria - Hierarquia de categorias com relacionamento pai/filho

cliente - Cadastro completo de clientes

produto - Catálogo de produtos com controle de estoque

pedido - Gestão de vendas com múltiplos status

itens_pedido - Itens dos pedidos com snapshots de preço

Gerenciamento de Conexões:
SQLiteConnection.java

Usuário Administrador Padrão:
Usuário: admin

Email: admin@local

Senha: admin123

Criado automaticamente no primeiro acesso


Telas Disponíveis
Login - Autenticação de usuários

Dashboard - Painel principal com métricas

Principal - Menu principal de navegação

Cadastros - Gestão de registros básicos

Categorias - CRUD de categorias

Clientes - Gestão de clientes

Produtos - Catálogo de produtos

Pedidos - Processamento de vendas

Usuários - Administração de usuários

Relatórios - Geração de relatórios

Shell - Console administrativo

