# mongodb-atlas-task-service

Task CRUD microservice backed by MongoDB Atlas. Documents its API with OpenAPI and caches reads with Caffeine.

## Stack
Java 21 · Spring Boot 4.1 · Spring Data MongoDB · Caffeine · springdoc-openapi 3 · MapStruct · Lombok

## Run
The Mongo connection string is never committed. `application.yml` reads it from `MONGODB_URI`, and
also imports an optional `application-local.yml` from the project root (git-ignored) if present:

```yaml
spring:
  mongodb:
    uri: mongodb+srv://<user>:<password>@<cluster>.mongodb.net/tasks?retryWrites=true&w=majority
```

```bash
./mvnw spring-boot:run
```

The service listens on `http://localhost:8083`. Your current IP must be on the Atlas
**Network Access** list, otherwise the TLS handshake is rejected and every request times out with a 500.

## Endpoints
| Method | Path                        | Description            | Success |
|--------|-----------------------------|------------------------|---------|
| POST   | `/v1/tasks`                 | Create a task          | 201     |
| GET    | `/v1/tasks`                 | List all tasks         | 200     |
| GET    | `/v1/tasks/{id}`            | Get one task           | 200     |
| GET    | `/v1/tasks/status/{status}` | List by completed flag | 200     |
| PUT    | `/v1/tasks/{id}`            | Update a task          | 200     |
| DELETE | `/v1/tasks/{id}`            | Delete a task          | 204     |

Errors are mapped by `GlobalExceptionHandler` to an `ErrorResponse` body: 404 for an unknown id,
400 for validation failures (with per-field messages).

## API docs (OpenAPI)
`springdoc-openapi-starter-webmvc-ui` generates the spec from the controllers at runtime —
paths, verbs, request/response schemas and Jakarta validation constraints are all inferred,
so a new endpoint shows up without extra annotations.

| URL                                      | What it is          |
|------------------------------------------|---------------------|
| `http://localhost:8083/swagger-ui.html`  | Swagger UI          |
| `http://localhost:8083/v3/api-docs`      | OpenAPI spec (JSON) |
| `http://localhost:8083/v3/api-docs.yaml` | OpenAPI spec (YAML) |

Use **Try it out** in the UI to hit the live service. Disable both in production with
`springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.enabled=false`.

## Caching
Reads are cached in-memory with Caffeine to avoid a round trip to Atlas on every request.
`@EnableCaching` lives in `config/CacheConfig`, and the annotations sit on `TaskServiceImpl`:

| Cache       | Keys                       | Written by                                           |
|-------------|----------------------------|------------------------------------------------------|
| `tasks`     | task id                    | `getTask` (`@Cacheable`), `updateTask` (`@CachePut`) |
| `taskLists` | `all`, `status:true/false` | `getAllTasks`, `getTaskByStatus`                     |

Every write evicts `taskLists` entirely (`allEntries = true`), since there is no way to tell which
list keys went stale. `updateTask` uses `@CachePut` so the per-id entry is refreshed with the value
it already has in hand, without a second read, while `deleteTask` evicts that id.

```yaml
spring:
  cache:
    type: caffeine
    cache-names: tasks,taskLists
    caffeine:
      spec: maximumSize=500,expireAfterWrite=60s,recordStats
```

Caching is proxy-based, so it only applies to calls that cross a bean boundary — a `this.getTask(...)`
call from inside `TaskServiceImpl` would bypass it.

## Tests
```bash
./mvnw clean verify
```
Unit tests only, no Mongo required. `TaskCacheTest` boots a minimal context (service + cache config,
repository mocked) and asserts the repository is hit once across two service calls, which is what
proves the cache is actually wired.
