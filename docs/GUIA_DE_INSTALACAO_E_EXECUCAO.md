# ⚙️ Guia de Instalação, Configuração e Execução - BeautySalon

Este guia fornece instruções completas para preparar o ambiente de desenvolvimento, configurar as variáveis e o banco de dados, compilar e executar o sistema **BeautySalon** (`Proj_Studio`).

---

## 📌 Sumário

1. [Requisitos de Ambiente](#1-requisitos-de-ambiente)
2. [Clonagem e Preparação do Projeto](#2-clonagem-e-preparação-do-projeto)
3. [Configuração do Banco de Dados](#3-configuração-do-banco-de-dados)
   * [3.1. Opção A: PostgreSQL Local (Recomendado para Dev)](#31-opção-a-postgresql-local-recomendado-para-dev)
   * [3.2. Opção B: Supabase PostgreSQL (Cloud)](#32-opção-b-supabase-postgresql-cloud)
4. [Configuração do Diretório de Uploads](#4-configuração-do-diretório-de-uploads)
5. [Compilação e Execução](#5-compilação-e-execução)
   * [5.1. Execução com Maven Wrapper](#51-execução-com-maven-wrapper)
   * [5.2. Empacotamento e Execução do JAR (.jar)](#52-empacotamento-e-execução-do-jar-jar)
6. [Configuração em IDEs (VS Code / IntelliJ IDEA / Eclipse)](#6-configuração-em-ides-vs-code--intellij-idea--eclipse)
7. [Tabela de Propriedades de Configuração](#7-tabela-de-propriedades-de-configuração)
8. [Troubleshooting (Resolução de Problemas Comuns)](#8-troubleshooting-resolução-de-problemas-comuns)

---

## 1. Requisitos de Ambiente

Antes de iniciar, certifique-se de ter instalado em sua estação de trabalho:

| Requisito | Versão Mínima | Finalidade |
| :--- | :---: | :--- |
| **Java Development Kit (JDK)** | **25 LTS** | Compilação e execução da aplicação |
| **Apache Maven** | 3.8+ | Gerenciamento de dependências *(opcional, o wrapper já está incluso)* |
| **PostgreSQL** | 14.x+ | Banco de dados relacional (caso utilize instalação local) |
| **Git** | 2.x+ | Controle de versionamento |

Para validar suas versões no terminal:
```bash
java -version
javac -version
git --version
```

> [!IMPORTANT]
> Certifique-se de que a variável de ambiente `JAVA_HOME` aponte para a instalação do JDK 25 (e não versões legadas como JDK 8, 11 ou 17).

---

## 2. Clonagem e Preparação do Projeto

1. Abra o terminal e clone o repositório em seu diretório de trabalho:
```bash
git clone <url-do-repositorio>
cd Proj_Studio
```

2. Verifique se os arquivos executáveis do Maven Wrapper estão presentes:
   * Windows: `mvnw.cmd`
   * Linux / macOS: `mvnw` (dê permissão de execução com `chmod +x mvnw`)

---

## 3. Configuração do Banco de Dados

As configurações de conexão residem no arquivo:
📂 `src/main/resources/application.properties`

### 3.1. Opção A: PostgreSQL Local (Recomendado para Dev)

1. Crie o banco de dados no seu PostgreSQL local via `psql` ou **pgAdmin**:
```sql
CREATE DATABASE salao_db;
```

2. No arquivo `application.properties`, comente a URL da nuvem e descomente a configuração local:
```properties
# Configuração PostgreSQL Local
spring.datasource.url=jdbc:postgresql://localhost:5432/salao_db
spring.datasource.username=postgres
spring.datasource.password=sua_senha_local
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 3.2. Opção B: Supabase PostgreSQL (Cloud)

Caso utilize a conexão remota com o Supabase:
```properties
spring.datasource.url=jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:6543/postgres
spring.datasource.username=postgres.bmqgiaegjalumhxlrirj
spring.datasource.password=sua_senha_supabase
spring.datasource.driver-class-name=org.postgresql.Driver
```

> [!TIP]
> Em ambientes de produção, evite manter senhas abertas no arquivo. Utilize variáveis de ambiente do sistema operacional, como:
> `export SPRING_DATASOURCE_PASSWORD=minhasenha`

---

## 4. Configuração do Diretório de Uploads

O sistema armazena imagens de serviços e fotos de perfil dos operadores no disco local:
* No Windows, certifique-se de que a pasta `uploads/` na raiz do projeto (ou `C:/beautysalon/uploads/`) exista ou tenha permissão de gravação.
* O sistema cria a pasta automaticamente na primeira execução através de `Files.createDirectories()`.

---

## 5. Compilação e Execução

### 5.1. Execução com Maven Wrapper

#### No Windows (PowerShell ou Prompt de Comando):
```powershell
.\mvnw.cmd spring-boot:run
```

#### No Linux / macOS:
```bash
./mvnw spring-boot:run
```

O Spring Boot inicializará os módulos, criará as tabelas do banco de dados (caso não existam) e exibirá a mensagem de inicialização indicando a porta ativa.

Acesse o sistema no navegador:
👉 **[http://localhost:8081](http://localhost:8081)**

---

### 5.2. Empacotamento e Execução do JAR (.jar)

Para gerar o artefato executável pronto para distribuição ou deploy:

1. Limpe o build anterior e gere o novo pacote:
```bash
# Windows
.\mvnw.cmd clean package -DskipTests

# Linux/macOS
./mvnw clean package -DskipTests
```

2. O arquivo gerado estará localizado em `target/BeautySalon-0.0.1-SNAPSHOT.jar`.

3. Execute diretamente com a JVM:
```bash
java -jar target/BeautySalon-0.0.1-SNAPSHOT.jar
```

Você pode sobrescrever a porta e parâmetros diretamente na linha de comando:
```bash
java -Dserver.port=9090 -jar target/BeautySalon-0.0.1-SNAPSHOT.jar
```

---

## 6. Configuração em IDEs (VS Code / IntelliJ IDEA / Eclipse)

### IntelliJ IDEA
1. Abra a pasta `Proj_Studio` como projeto Maven (`Open -> Proj_Studio/pom.xml`).
2. Aguarde o download das dependências.
3. Habilite o processamento de anotações do **Lombok**:
   * `Settings/Preferences -> Build, Execution, Deployment -> Compiler -> Annotation Processors -> Enable annotation processing`.
4. Execute a classe principal `BeautySalonApplication.java`.

### VS Code
1. Instale a extensão oficial **Extension Pack for Java** e **Spring Boot Extension Pack**.
2. Certifique-se de ter instalada a extensão **Lombok Annotations Support for VS Code**.
3. Pressione `F5` ou utilize a aba *Spring Boot Dashboard* para iniciar a aplicação.

---

## 7. Tabela de Propriedades de Configuração

Principais propriedades configuradas em `application.properties`:

| Propriedade | Valor Padrão | Descrição |
| :--- | :--- | :--- |
| `server.port` | `8081` | Porta HTTP na qual o servidor Tomcat embutido escuta |
| `server.servlet.context-path` | `/` | Raiz do contexto web da aplicação |
| `spring.datasource.url` | `jdbc:postgresql://...` | JDBC URL para conexão com a base de dados |
| `spring.jpa.hibernate.ddl-auto` | `update` | Atualização automática do esquema relacional |
| `spring.jpa.show-sql` | `true` | Exibe no terminal as queries SQL executadas |
| `spring.thymeleaf.cache` | `false` | Desabilita cache de templates para recarga rápida em dev |
| `spring.servlet.multipart.max-file-size` | `5MB` | Tamanho máximo permitido por arquivo em upload |
| `management.endpoints.web.exposure.include` | `*` | Expõe endpoints do Spring Actuator para monitoramento |
| `logging.level.com.beautysalon` | `TRACE` | Nível detalhado de logs da aplicação |

---

## 8. Troubleshooting (Resolução de Problemas Comuns)

### A. Erro: "Port 8081 was already in use"
* **Causa**: Outra instância do projeto ou outro aplicativo já está rodando na porta 8081.
* **Solução**:
  1. Altere a porta no `application.properties` para `server.port=8082`, ou:
  2. Localize e finalize o processo que está ocupando a porta:
     * Windows (PowerShell):
       ```powershell
       Get-Process -Id (Get-NetTCPConnection -LocalPort 8081).OwningProcess | Stop-Process
       ```

### B. Erro: "Connection to localhost:5432 refused"
* **Causa**: O serviço do PostgreSQL não está em execução ou o banco `salao_db` não foi criado.
* **Solução**:
  1. Inicie o serviço do PostgreSQL (via `services.msc` no Windows ou `sudo systemctl start postgresql` no Linux).
  2. Conecte-se e confirme a existência da base `salao_db`.

### C. Erro: "password authentication failed for user"
* **Causa**: Credenciais inválidas no `application.properties`.
* **Solução**: Verifique o usuário e senha configurados nas chaves `spring.datasource.username` e `spring.datasource.password`.

### D. Métodos Getters/Setters não encontrados em compilação
* **Causa**: O processador de anotações do **Lombok** não está ativo no compilador ou IDE.
* **Solução**: Certifique-se de que a IDE possui o plugin do Lombok instalado e que o `pom.xml` possui a configuração do `maven-compiler-plugin` com a tag `<annotationProcessorPaths>`.
