# Complemento - Configuração do pom.xml

## Configuração Base do Maven

Este arquivo complementa a **Task 01** com as configurações específicas do pom.xml.

---

## Properties Essenciais

```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <spring-boot.version>3.4.3</spring-boot.version>
    
    <!-- Versões de dependências -->
    <lombok.version>1.18.30</lombok.version>
    <postgresql.version>42.7.3</postgresql.version>
    <jjwt.version>0.12.5</jjwt.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
    <springdoc.version>2.3.0</springdoc.version>
    <flyway.version>10.10.0</flyway.version>
    <caffeine.version>3.1.8</caffeine.version>
    <thumbnailator.version>0.4.20</thumbnailator.version>
</properties>
```

---

## Packaging

```xml
<packaging>war</packaging>
```

---

## Parent

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.3</version>
    <relativePath/>
</parent>
```

---

## Dependências Principais

### Spring Boot Starters

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Data JPA (Hibernate) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Boot Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Spring Boot Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Spring Boot WebFlux (para WebClient) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    
    <!-- Spring Boot Actuator (monitoramento) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Spring Boot Cache -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    
    <!-- Tomcat (provided para WAR) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>
        <scope>provided</scope>
    </dependency>
```

### Banco de Dados

```xml
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>${postgresql.version}</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Flyway Migration -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
        <version>${flyway.version}</version>
    </dependency>
    
    <!-- Flyway PostgreSQL Specific -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
        <version>${flyway.version}</version>
        <scope>runtime</scope>
    </dependency>
```

### Lombok

```xml
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
        <scope>provided</scope>
    </dependency>
```

### JWT

```xml
    <!-- JWT API -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>${jjwt.version}</version>
    </dependency>
    
    <!-- JWT Implementation -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>${jjwt.version}</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- JWT Jackson -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>${jjwt.version}</version>
        <scope>runtime</scope>
    </dependency>
```

### Documentação OpenAPI

```xml
    <!-- Springdoc OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>${springdoc.version}</version>
    </dependency>
```

### Mapper (**MapStruct** - obrigatório)

```xml
    <!-- MapStruct (mapeamento compile-time, obrigatório) -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>
```

### Compressão de Imagem

```xml
    <!-- Thumbnailator (compressão/redimensionamento de imagens) -->
    <dependency>
        <groupId>net.coobird</groupId>
        <artifactId>thumbnailator</artifactId>
        <version>${thumbnailator.version}</version>
    </dependency>
```

### Cache (Blacklist JWT + Consultas)

```xml
    <!-- Caffeine Cache (blacklist de tokens JWT + cache de consultas) -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
        <version>${caffeine.version}</version>
    </dependency>
```

### Testes

```xml
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Spring Security Test -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- H2 Database (para testes) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- TestContainers PostgreSQL -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <version>1.19.7</version>
        <scope>test</scope>
    </dependency>
    
    <!-- TestContainers JUnit -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>1.19.7</version>
        <scope>test</scope>
    </dependency>
    
    <!-- RestAssured -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Build Configuration

```xml
<build>
    <finalName>${project.artifactId}</finalName>
    
    <plugins>
        <!-- Spring Boot Maven Plugin -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
        
        <!-- Maven Compiler Plugin -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>21</source>
                <target>21</target>
                <release>21</release>
                <annotationProcessorPaths>
                    <!-- Lombok -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <!-- MapStruct (se usar) -->
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${mapstruct.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
        
        <!-- Maven WAR Plugin -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-war-plugin</artifactId>
            <version>3.4.0</version>
            <configuration>
                <failOnMissingWebXml>false</failOnMissingWebXml>
            </configuration>
        </plugin>
        
        <!-- Flyway Maven Plugin -->
        <plugin>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-maven-plugin</artifactId>
            <version>${flyway.version}</version>
            <configuration>
                <url>jdbc:postgresql://localhost:5432/msimagens</url>
                <user>postgres</user>
                <password>postgres</password>
            </configuration>
        </plugin>
        
        <!-- JaCoCo Plugin (cobertura de testes) -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>jacoco-check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>PACKAGE</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.80</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## Comandos Maven Úteis

```bash
# Compilar o projeto
mvn clean compile

# Rodar testes
mvn test

# Gerar WAR
mvn clean package

# Pular testes e gerar WAR
mvn clean package -DskipTests

# Rodar Flyway migrations
mvn flyway:migrate

# Limpar banco (cuidado!)
mvn flyway:clean

# Gerar relatório de cobertura
mvn clean test jacoco:report

# Instalar no repositório local
mvn clean install

# Rodar a aplicação (development)
mvn spring-boot:run
```

---

## Configuração do ServletInitializer

Como o projeto é empacotado como WAR, precisa de uma classe para inicialização:

```java
package com.empresa.msimagens;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MsImagensApplication.class);
    }
}
```

---

## Observações Importantes

1. **Java 21:** Certifique-se de ter o JDK 21 instalado
2. **Lombok:** Configure sua IDE para processar anotações Lombok
3. **MapStruct vs ModelMapper:** 
   - **MapStruct:** Obrigatório neste projeto (compile-time, melhor performance, erros em compilação)
   - Integração com Lombok via `lombok-mapstruct-binding`
4. **WAR Deployment:** 
   - O arquivo WAR será criado em `target/${artifactId}.war`
   - Deploy em Tomcat 10+ (suporta Jakarta EE)
5. **Flyway:** 
   - Migrations em `src/main/resources/db/migration/`
   - Nomenclatura: `V1__description.sql`, `V2__description.sql`

---

**Próximo Passo:** Após configurar o pom.xml, seguir para Task 02 (Camada de Domínio)
