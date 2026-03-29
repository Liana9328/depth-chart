# NFL Depth Chart

Spring Boot 3 API for managing team depth charts by sport/team/position.  
Tech stack: Spring Web, Spring Data JPA, Spring Cache, H2, OpenAPI (Swagger), Maven.

## 1) Build and Run

### Prerequisites

- Java: **JDK 21+** (project is configured with `java.version=21`)
- OS: macOS, Linux, or Windows
- Maven install: **optional** (Maven Wrapper is included)

### OS setup notes

- **macOS / Linux**
  - Ensure `java -version` is 21+
  - Use `./mvnw ...`
- **Windows (PowerShell / CMD)**
  - Ensure `java -version` is 21+
  - Use `mvnw.cmd ...`

### Commands

Build + test:

```bash
./mvnw clean verify
```

Run locally:

```bash
./mvnw spring-boot:run
```

App endpoints:

- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:depthchart`
  - User: `sa`
  - Password: *(empty)*

## 2) How to Test Locally

- Automated tests:

```bash
./mvnw test
```

- Manual API verification:
  - Use `TESTING_CURL.md` for complete curl scenarios
  - It covers all required use cases and all available APIs, with expected status/results

- Swagger verification:
  - Open `http://localhost:8080/swagger-ui.html`
  - Execute endpoints interactively and compare with expected behavior in `TESTING_CURL.md`

- H2 verification:
  - Open `http://localhost:8080/h2-console`
  - Connect using `jdbc:h2:mem:depthchart`
  - Inspect `players` and `depth_chart_entries` tables

## 3) Assumptions

- This is a **light-write / heavy-read** system, so the design prioritizes read performance.
- Read-heavy traffic is handled with **caching** (`depthChart`, `fullChart`) and proper cache eviction on writes.
- H2 in-memory database is used to simulate persistence with separate tables for players and depth entries.
- `sport` and `team` are path variables in APIs to keep data model and endpoints flexible for multiple leagues/teams.
- `position_depth` is treated as **0-based** (`0` = starter);

## 4) Additional Notes

- `bulk-add` is a convenience endpoint to add multiple players.

