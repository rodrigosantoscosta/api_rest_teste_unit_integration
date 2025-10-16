# api_rest_teste_unit_integration
Repositório com projeto de API REST completo com Spring Boot para testes unitários e para testes de integração já com swagger.

# Java REST API

Este projeto é uma API REST simples em Java, desenvolvida com Spring Boot. Ele disponibiliza endpoints para gerenciar itens e oferece suporte a respostas nos formatos JSON e XML.

## Estrutura do Projeto

```
java-rest-api
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── demo
│   │   │               ├── DemoApplication.java
│   │   │               ├── controller
│   │   │               │   └── ApiController.java
│   │   │               ├── exception
│   │   │               │   └── RestExceptionHandler.java
│   │   │               ├── model
│   │   │               │   └── Item.java
│   │   │               └── service
│   │   │                   └── ItemService.java
│   │   └── resources
│   │       └── application.properties
│   └── test
│       └── java
│           └── com
│               └── example
│                   └── demo
│                       └── service
│                       │   └── ItemServiceTest.java
│                       └── ApiControllerTest.java
└── README.md
```

## Instruções de Configuração

1. **Clonar o repositório::**
   ```
   git clone <repository-url>
   cd java-rest-api
   ```

2. **Compilar o projeto:**
   Certifique-se de que o Maven esteja instalado e, em seguida, execute:
   ```
   mvn clean install
   ```

3. **Executar a aplicação:**
   Você pode iniciar a aplicação utilizando:
   ```
   mvn spring-boot:run
   ```

4. **Acessar a API:**
   A API estará disponível em `http://localhost:8080/api/items`.
5.  **Documentação da API:**
   A documentação via swagger está diponível em `http://localhost:8080/swagger-ui/index.html`.

## Exemplos de Uso

- **Obter todos os itens:**
  ```
  GET /api/items
  ```

- **Obter um item por ID:**
  ```
  GET /api/items/{id}
  ```

- **Criar um novo item:**
  ```
  POST /api/items
  Content-Type: application/json

  {
      "name": "Item Name",
      "description": "Item Description"
  }
  ```

## Testes

Testes unitários estão incluídos no projeto para validar a funcionalidade da API. Você pode executá-los utilizando:
```
mvn test
```

Isso executará os testes definidos em `ApiControllerTest.java`, os quais verificam as respostas corretas em JSON e XML, além de validar os códigos de status HTTP.

## Testes unitários

Cenários de Testes de exceção
Corpo da Requisição Vazio - Verificar como a API reage a um POST sem dados. - Um erro de "Bad Request" (Status 400).
Corpo da Requisição com JSON Inválido - Verificar o comportamento com um JSON malformado. - Um erro de "Bad Request" (Status 400).

Cenários de Testes "mundo real"
Buscar todos os itens quando a lista está vazia - Garante que a API retorne uma resposta graciosa (uma lista vazia ``) em vez de um erro ou null quando não há dados.
Buscar todos os itens quando existem dados - Valida o "caminho feliz" para a listagem, verificando a estrutura do array JSON e o número de itens.
Verificar a estrutura completa da resposta de erro - Além de checar o status `400 Bad Request`, vamos garantir que o corpo do erro contenha as mensagens de validação corretas para os campos específicos.


