# Job-Forge

A Spring Boot job processing service that handles asynchronous job execution via RabbitMQ, with support for CSV file processing and more to come in the future.

## Architecture

```
┌─────────────┐        ┌──────────────────────────────────────────────────────┐
│   Client    │        │                     Job-Forge                        │
│             │        │                                                      │
│  POST /jobs │──────► │  JobController ──► JobService ──► JobRepository      │
│  GET  /jobs │◄────── │                        │                             │
│  POST /csv  │        │                        │ publish                     │
└─────────────┘        │                        ▼                             │
                       │                  JobPublisher                        │
                       │                        │                             │
                       └────────────────────────┼─────────────────────────────┘
                                                │
                                    ┌───────────▼───────────┐
                                    │       RabbitMQ        │
                                    │  Exchange: jobforge   │
                                    │  Queue:    jobforge   │
                                    └───────────┬───────────┘
                                                │
                       ┌────────────────────────┼────────────────────────────┐
                       │                        ▼                            │
                       │                  JobWorker                          │
                       │                        │                            │
                       │                  JobExecutor                        │
                       │                        │                            │
                       │                  JobDispatcher                      │
                       │                   /         \                       │
                       │    CsvProcessJobHandler   (future handlers)         │
                       │                   │                                 │
                       │            CsvFileRepository                        │
                       │                   │                                 │
                       │              PostgreSQL                             │
                       └─────────────────────────────────────────────────────┘
```

## Tech Stack

| Component        | Technology                     |
|------------------|-------------------------------|
| Framework        | Spring Boot 4.0                |
| Language         | Java 25                        |
| Database         | PostgreSQL 17                  |
| Migrations       | Flyway                         |
| Message Broker   | RabbitMQ 3                     |
| ORM              | Spring Data JPA / Hibernate    |
| Build Tool       | Gradle (Kotlin DSL)            |
| Containerization | Docker / Docker Compose        |
| Testing          | JUnit 5, Mockito, AssertJ, Awaitility |

## Running with Docker Compose

```bash
docker compose up
```

The app, PostgreSQL, and RabbitMQ will all start automatically. Flyway runs migrations on startup.

### Environment Variables

All configuration can be overridden via environment variables. Defaults work out of the box.

| Variable                     | Default     | Description                        |
|------------------------------|-------------|------------------------------------|
| `DB_HOST`                    | `postgres`  | PostgreSQL host                    |
| `DB_PORT`                    | `5432`      | PostgreSQL port                    |
| `DB_NAME`                    | `jobforge`  | Database name                      |
| `DB_USERNAME`                | `jobforge`  | Database user                      |
| `DB_PASSWORD`                | `password`  | Database password                  |
| `RABBITMQ_HOST`              | `rabbitmq`  | RabbitMQ host                      |
| `RABBITMQ_PORT`              | `5672`      | RabbitMQ AMQP port                 |
| `RABBITMQ_USERNAME`          | `guest`     | RabbitMQ user                      |
| `RABBITMQ_PASSWORD`          | `guest`     | RabbitMQ password                  |
| `DB_HOST_PORT`               | `5432`      | Host port for PostgreSQL           |
| `RABBITMQ_HOST_PORT`         | `5672`      | Host port for RabbitMQ AMQP        |
| `RABBITMQ_MANAGEMENT_HOST_PORT` | `15672`  | Host port for RabbitMQ management  |
| `APP_HOST_PORT`              | `8080`      | Host port for the application      |

To avoid port conflicts with existing services, create a `.env` file:

```env
DB_HOST_PORT=5433
RABBITMQ_HOST_PORT=5673
APP_HOST_PORT=8081
```

## API Documentation

### Interactive Docs (Swagger UI)

Swagger UI is available at `<host>/swagger-ui/index.html` when the app is running (e.g. `http://localhost:8080/swagger-ui/index.html` locally, or your VPS address if deployed).

The raw OpenAPI spec is served at `<host>/v3/api-docs`.

---

### Jobs

#### Create a Job

```
POST /api/v1/jobs
Content-Type: application/json

{
  "type": "CSV_PROCESS",
  "maxAttempts": 3,
  "scheduledFor": "2025-12-01T10:00:00Z"  // optional
}
```

Response `201 Created`:
```json
{
  "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "QUEUED"
}
```

---

#### Get a Job

```
GET /api/v1/jobs/{id}
```

Response `200 OK`:
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "type": "CSV_PROCESS",
  "status": "COMPLETED",
  "attempts": 1,
  "maxAttempts": 3,
  "createdAt": "2025-01-01T10:00:00Z",
  "updatedAt": "2025-01-01T10:00:05Z",
  "scheduledFor": null,
  "result": "{...}",
  "error": null
}
```

---

#### List Jobs

```
GET /api/v1/jobs?status=QUEUED&type=CSV_PROCESS
```

Both `status` and `type` are optional filters. Omit either to return all values for that dimension.

Response `200 OK`: array of job view objects (same shape as above).

---

#### Cancel a Job

Only `QUEUED` jobs can be cancelled. A cancelled job is deleted.

```
POST /api/v1/jobs/{id}/cancel
```

Response `204 No Content`

---

### CSV Jobs

#### Upload and Process a CSV File

```
POST /api/v1/csv/jobs
Content-Type: multipart/form-data

file=<csv file>
delimiter=,          // optional, default ","
hasHeader=true       // optional, default true
maxAttempts=3        // optional, default 5
```

Response `201 Created`:
```json
{
  "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "QUEUED"
}
```

---

#### Get CSV Job Summary

Job must be `COMPLETED`.

```
GET /api/v1/csv/jobs/{id}/summary
```

Response `200 OK`:
```json
{
  "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "COMPLETED",
  "rowsProcessed": 150,
  "rowsFailed": 2,
  "columns": 4,
  "header": ["name", "age", "city", "score"],
  "processedAt": "2025-01-01T10:00:05Z",
  "originalName": "data.csv"
}
```

---

#### Get CSV Job Raw Result

Job must be `COMPLETED`. Returns the full JSON result including all parsed rows.

```
GET /api/v1/csv/jobs/{id}/result
```

Response `200 OK`: raw JSON string containing `rows`, `header`, metadata.

---

### Job Statuses

| Status      | Description                                         |
|-------------|-----------------------------------------------------|
| `QUEUED`    | Job is waiting to be picked up                      |
| `RUNNING`   | Job is currently being executed                     |
| `COMPLETED` | Job finished successfully                           |
| `FAILED`    | Job exceeded max attempts                           |
| `CANCELLED` | Job was manually cancelled (deleted from DB)        |

### Error Responses

| HTTP Status | Condition                                      |
|-------------|------------------------------------------------|
| `400`       | Invalid request body or bad argument           |
| `404`       | Job not found                                  |
| `409`       | Job is not in a valid state for the operation  |

---

## Retry & Failure Behavior

When a job handler throws an exception:

1. `attempts` is incremented
2. If `attempts < maxAttempts`: job is re-queued with exponential backoff and republished to RabbitMQ
3. If `attempts >= maxAttempts`: job status is set to `FAILED`

**Backoff schedule:**

| Attempt | Delay  |
|---------|--------|
| 1       | 2s     |
| 2       | 5s     |
| 3       | 15s    |
| 4       | 30s    |
| 5+      | 60s    |

---

## Design Tradeoffs

### RabbitMQ vs. Database Polling

The original design used database polling with pessimistic locking (`SELECT ... FOR UPDATE`) to claim jobs. While this approach is simple and requires no additional infrastructure, it does not scale well. Each worker polls the database on an interval regardless of whether there is work to do.

The current design publishes a message to RabbitMQ when a job is submitted. Workers consume messages via `@RabbitListener`, eliminating poll latency and reducing database load. The trade-off is that RabbitMQ becomes a required infrastructure dependency.

The database schema still retains fields from the polling era (`locked_at`, `locked_by`, `next_run_at`) to support future hybrid scenarios, such as scheduled jobs or delayed retries that cannot be represented purely with message queuing.

### Pessimistic Locking (retained for scheduled jobs)

The `findClaimCandidates` query uses `SELECT ... FOR UPDATE` to prevent two workers from claiming the same job. While this is not used in the primary RabbitMQ flow, it remains available for any future polling-based reclaim of stuck or scheduled jobs.

---

## Future Improvements

- **Scheduled job reclaim**: A background task to re-queue jobs that are stuck in `RUNNING` (e.g. worker crashed mid-execution)
- **Dead letter queue**: Route permanently failed jobs to a DLQ for inspection
- **Job priorities**: Priority queue routing in RabbitMQ
- **Additional job types**: Extend `JobType` and register new `JobHandler` implementations
- **Authentication**: Secure the API with JWT or API keys
- **Metrics**: Expose job throughput, failure rate, and queue depth via Actuator / Prometheus