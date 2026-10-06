# core/

The Java 21 / Spring Boot 4.1.x service, as a Gradle multi-module build. It owns ingestion and
the idempotent write, schema migrations, the query API and the live tail, and — from later
phases — incidents, correlation and identity. It holds no telemetry state of its own:
everything lives in Kafka, ClickHouse or PostgreSQL.

- **Owner:** @merttemiz (platform)
- **Filled by:** WP3 — Core Skeleton (deliverable D5); then WP8 (storage and migrations),
  WP9 (log ingestion), WP10 (query API) and WP11 (live tail).

Empty today.
