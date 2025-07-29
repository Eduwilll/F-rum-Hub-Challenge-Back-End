# 🗣️ FórumHub API

Uma API REST completa para gerenciamento de fórum de discussões, desenvolvida com Spring Boot 3.5.4 e Java 17.

## 📋 Sobre o Projeto

O FórumHub é uma API REST que replica o funcionamento de um fórum de discussões, similar ao fórum da Alura. Os usuários podem criar tópicos, responder a discussões, marcar soluções e gerenciar o status dos tópicos. A API implementa autenticação JWT, autorização baseada em roles e todas as operações CRUD necessárias.

### ✨ Funcionalidades Principais

- **Autenticação e Autorização**: Sistema completo com JWT e roles (USUARIO, MODERADOR, ADMIN)
- **Gerenciamento de Tópicos**: CRUD completo com validações e prevenção de duplicatas
- **Sistema de Respostas**: Usuários podem responder tópicos e marcar soluções
- **Controle de Status**: Tópicos podem ser abertos/fechados automaticamente
- **Documentação Interativa**: Swagger UI integrado
- **Testes Abrangentes**: Testes unitários e de integração com TestContainers

## 🛠️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.5.4**
- **Spring Security** (Autenticação JWT)
- **Spring Data JPA** (Persistência de dados)
- **MySQL** (Banco de dados)
- **Flyway** (Migrações de banco)
- **Lombok** (Redução de boilerplate)
- **SpringDoc OpenAPI** (Documentação)
- **JUnit 5 + Mockito** (Testes unitários)
- **TestContainers** (Testes de integração)

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas bem definida:

```
┌─────────────────┐
│   Controllers   │ ← Endpoints REST, validação de entrada
├─────────────────┤
│    Services     │ ← Lógica de negócio, regras de autorização
├─────────────────┤
│  Repositories   │ ← Acesso a dados, queries customizadas
├─────────────────┤
│    Entities     │ ← Entidades JPA, mapeamento do banco
└─────────────────┘
```

## 📊 Modelo de Dados

### Entidades Principais

- **User**: Usuários do sistema com perfis de acesso
- **Profile**: Perfis de usuário (USUARIO, MODERADOR, ADMIN)
- **Topic**: Tópicos de discussão
- **Response**: Respostas aos tópicos
- **Course**: Cursos para categorização dos tópicos

### Relacionamentos

- User ↔ Profile (N:N)
- User → Topic (1:N)
- User → Response (1:N)
- Course → Topic (1:N)
- Topic → Response (1:N)

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Docker (opcional, para testes)

### Configuração do Banco de Dados

1. Crie um banco MySQL:
```sql
CREATE DATABASE forum_db;
CREATE USER 'forum_user'@'localhost' IDENTIFIED BY 'forum_pass';
GRANT ALL PRIVILEGES ON forum_db.* TO 'forum_user'@'localhost';
```

2. Configure as variáveis de ambiente (opcional):
```bash
export DB_USERNAME=forum_user
export DB_PASSWORD=forum_pass
export JWT_SECRET=meuSegredoSuperSecreto123456789012345678901234567890
```

### Executando a Aplicação

1. Clone o repositório:
```bash
git clone <url-do-repositorio>
cd forum-api
```

2. Execute a aplicação:
```bash
mvn spring-boot:run
```

3. A API estará disponível em: `http://localhost:8080`

### Executando os Testes

```bash
# Testes unitários
mvn test

# Testes de integração (requer Docker)
mvn verify
```

## 📚 Documentação da API

### Swagger UI
Acesse a documentação interativa em: `http://localhost:8080/swagger-ui.html`

### Endpoints Principais

#### Autenticação
- `POST /auth/register` - Registrar novo usuário
- `POST /auth/login` - Fazer login

#### Tópicos
- `GET /topicos` - Listar tópicos (com paginação e filtros)
- `GET /topicos/{id}` - Obter tópico específico
- `POST /topicos` - Criar novo tópico
- `PUT /topicos/{id}` - Atualizar tópico
- `DELETE /topicos/{id}` - Deletar tópico
- `PUT /topicos/{id}/close` - Fechar tópico
- `PUT /topicos/{id}/open` - Reabrir tópico

#### Respostas
- `POST /topicos/{id}/respostas` - Criar resposta
- `GET /topicos/{id}/respostas` - Listar respostas do tópico
- `PUT /respostas/{id}/solucao` - Marcar como solução
- `DELETE /respostas/{id}` - Deletar resposta

## 🔐 Autenticação

A API utiliza JWT (JSON Web Tokens) para autenticação. Após o login, inclua o token no header:

```
Authorization: Bearer <seu-jwt-token>
```

### Exemplo de Uso

1. **Registrar usuário:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@email.com",
    "senha": "senha123"
  }'
```

2. **Fazer login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@email.com",
    "senha": "senha123"
  }'
```

3. **Criar tópico:**
```bash
curl -X POST http://localhost:8080/topicos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "titulo": "Dúvida sobre Spring Security",
    "mensagem": "Como configurar JWT no Spring Security?",
    "cursoId": 1
  }'
```

## 🧪 Testes

O projeto possui cobertura abrangente de testes:

### Testes Unitários
- **Entities**: Validação de regras de negócio
- **Repositories**: Queries customizadas
- **Services**: Lógica de negócio e autorização
- **Controllers**: Endpoints e validações

### Testes de Integração
- **API Completa**: Testes end-to-end com banco real
- **Segurança**: Autenticação e autorização
- **Cenários Complexos**: Fluxos completos de usuário

## 🔧 Configuração

### Profiles Disponíveis

- **default**: Configuração padrão
- **dev**: Configuração para desenvolvimento (logs detalhados)
- **test**: Configuração para testes

### Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|---------|
| `DB_USERNAME` | Usuário do banco | `forum_user` |
| `DB_PASSWORD` | Senha do banco | `forum_pass` |
| `JWT_SECRET` | Chave secreta JWT | `mySecretKey...` |

## 📈 Melhorias Futuras

- [ ] Sistema de notificações
- [ ] Upload de arquivos/imagens
- [ ] Sistema de votação em respostas
- [ ] Moderação automática de conteúdo
- [ ] API de estatísticas
- [ ] Cache com Redis
- [ ] Rate limiting

## 🤝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👨‍💻 Autor

Desenvolvido como parte do Challenge Back End da Alura.

---

⭐ Se este projeto foi útil para você, considere dar uma estrela no repositório!