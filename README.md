# AI Task Queue Project

## What this is
Distributed AI task queue with observability. Clients submit long-running AI jobs via REST APIs, workers process them via Kafka, with Redis rate limiting and Prometheus/Grafana monitoring.

## Stack
Java 26, Spring Boot 3.3, Kafka, Redis, PostgreSQL, Docker, WebSockets, Micrometer/Prometheus/Grafana

## Project structure
- controller/ - REST endpoints (JobController)
- service/ - Business logic (JobService, RateLimiterService)
- producer/ - Kafka producer (KafkaJobProducer)
- consumer/ - Kafka consumer/worker (KafkaJobConsumer)
- model/ - JPA entities (Job, JobStatus enum)
- repository/ - Spring Data JPA repos
- websocket/ - Real-time status push
- config/ - Kafka, Redis, WebSocket config

## Key features to build
1. POST /jobs - submit job with prompt, priority
2. GET /jobs/{id} - check status
3. Kafka async routing to worker pool
4. Redis sliding-window rate limiter
5. Retry with exponential backoff + idempotency keys
6. WebSocket status push to clients
7. Prometheus metrics via Actuator/Micrometer
8. Docker Compose for full stack