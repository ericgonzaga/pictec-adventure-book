# Adventure Book — Backend

REST API for interactive books and game sessions, built with Spring Boot, JPA, and file-based H2.

## Requirements

- JDK 25
- Maven (optional; the project includes the Maven Wrapper, `mvnw`)

Check your Java version:

```bash
java -version
```

## Run the application

```bash
./mvnw spring-boot:run
```

The API runs at `http://localhost:8080` (Spring’s default port).

### Configuration (`src/main/resources/application.properties`)

Before running the project, set `books.storage.dir` as valid directory on your machine.

## Build and run jar file

./mvnw clean package

java -jar target/adventure-book-0.0.1-SNAPSHOT.jar

## Tests

### All tests

./mvnw test

### A single test class

./mvnw test -Dtest=GameEngineTest

### A single test method

./mvnw test -Dtest=GameFlowTest#restart_abandonsInProgressSession

### Tests and build

./mvnw verify
