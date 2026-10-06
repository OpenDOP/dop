# DOP — Distributed Observability Platform

> **Work in progress.** This repository is in Phase 1 of an eight-phase plan. There is no
> release yet, nothing here is installable, and nothing below describes software that runs
> today. The first usable release is **v0.1.0 "Log Explorer"**, targeted for **11.12.2026**.

DOP is a self-hosted, open-source observability product. It ingests logs, traces and metrics
through OpenTelemetry, compresses log noise into patterns, checks the signals with explainable
statistical detectors, and correlates what it finds into incidents whose root-cause candidates
are ranked with evidence. Its difference is evidence-based incident correlation: storing
telemetry and drawing it is a solved problem, and DOP's value starts where independent
anomalies are joined into one incident and every claim about the cause can be clicked through
to the log lines, traces, patterns and deployments that support it. The deterministic engine is
the core of the product — a language-model layer comes only after v1.0 and explains evidence
that already exists. DOP is not a Grafana: there is no dashboard editor and no PromQL-like
query language, and it is designed to run next to an existing Prometheus and Grafana
installation rather than to replace them. The goal is a product a stranger can install in ten
minutes on a laptop and an SRE team can operate on a cluster at v1.0.

## Architecture

Figure 2 of the [architecture reference](docs/architecture/architecture-reference.md),
reproduced verbatim:

```
Figure 2 - Components and data flow (corrected in this edition)

  applications (OpenTelemetry SDK)
        |  OTLP gRPC 4317 / HTTP 4318
        v
  +--------------------------+
  | OpenTelemetry Collector  |  otlp receiver -> memory_limiter -> batch
  | (bundled)                |  -> redaction -> kafka exporter
  +--------------------------+
        |
        v
  +--------------------------------------------------------------------+
  | Event backbone - Kafka API (Redpanda by default, Kafka KRaft)      |
  | raw OTLP topics | dead-letter topics | normalised topics (P3)      |
  | analytics result topics (P3) | pattern-state topic (P3)            |
  +--------------------------------------------------------------------+
     | raw        ^ normalised            | normalised      ^ patterns,
     v            | events (P3)           v events (P3)     | anomalies
  +------------------------------+      +------------------------------+
  | core (Java 21, Spring Boot)  |      | analytics (Python 3.12)      |
  | ingestion  telemetry  query  |      | pattern mining, detectors    |
  | incident  identity  platform |      +------------------------------+
  +------------------------------+                     :
     |              |           ^                      : baseline read only:
     | write, read  | control   | REST + SSE           : per-minute
     v              v plane     |                      : pre-aggregates (P3)
  +------------+ +------------+ +-------------------+  v
  | ClickHouse | | PostgreSQL | | web (static SPA   | (ClickHouse)
  | telemetry  | | control    | | behind Nginx)     |
  +------------+ +------------+ +-------------------+
```

## What each release adds

| Release | Signal or capability |
|:--------|:---------------------|
| v0.1 | Logs end to end: ingest, store, search, live tail |
| v0.2 | Traces, log-to-trace navigation, service map |
| v0.3 | Pattern mining, four detectors, Anomaly Center |
| v0.4 | Incidents, correlation, root-cause ranking, notifications |
| v0.5 | Users, roles, tokens, OIDC, tenant isolation, audit |
| v0.6 | Metrics ingest, Metrics Explorer, metric evidence |
| v1.0 | Helm, backup and upgrade, benchmarks, signed images |
| v1.x | AI explanation, advanced detectors, ecosystem, governance |

Dates exist for Phase 1 only. See [docs/roadmap.md](docs/roadmap.md).

## Quickstart — TARGET, NOT YET AVAILABLE

**Draft.** None of this works today. No image has been published, no Compose file exists, and
the commands below are the shape the quickstart is intended to take, not a tested procedure.
The Compose stack is built in WP5 and the published images arrive with v0.1.0. The user-facing
address of the interface is fixed in WP5 and is deliberately left unwritten here.

```
# intended shape — does not work yet
curl -O https://github.com/OpenDOP/dop/releases/download/v0.1.0/docker-compose.yml
docker compose up -d
```

Point an OpenTelemetry SDK or Collector at the bundled Collector — OTLP gRPC on port `4317`
or OTLP HTTP on port `4318` — and the logs are intended to appear in the Log Explorer.

The target that governs the design: on a clean machine with no cached images, a new user sees
their own telemetry within **ten minutes**, on a laptop with **8 GB of RAM**.

## No authentication before v0.5

> **Warning.** Releases **v0.1 through v0.4 have no authentication, no users, no roles and no
> tokens.** Anyone who can reach the interface or the API can read all stored telemetry.
> Authentication, RBAC, API tokens, OIDC and tenant isolation arrive in **v0.5** (Phase 5).
>
> The quickstart profile is intended to publish the interface, the API and the OTLP ports on
> `127.0.0.1` only and to publish no data-store port at all. That is a safety default, not a
> security feature. Do not expose a v0.1–v0.4 installation to an untrusted network.

## Licence and contributing

- [LICENSE](LICENSE) — Apache-2.0. Contributions are signed off with the DCO; there is no CLA.
- [CONTRIBUTING.md](CONTRIBUTING.md) — how to ask, branch and pull-request rules, Conventional
  Commits, DCO, the AI coding assistant policy.
- [SECURITY.md](SECURITY.md) — how to report a vulnerability privately.
- [docs/roadmap.md](docs/roadmap.md) — the eight phases and what each must prove.

Questions and ideas belong in
[Discussions](https://github.com/OpenDOP/dop/discussions), not in issues.
