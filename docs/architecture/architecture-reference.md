# DOP — Open-Source Product Architecture and Technical Reference

**Distributed Observability Platform** — open-source, self-hosted

| | |
|:--|:--|
| Edition | Second edition — 05.10.2026. Supersedes the first edition of 29.09.2026 |
| Aligned with | Master roadmap v1.0 (29.09.2026); Phase 1 Reference Roadmap, Revision 1 (05.10.2026) |
| Repository | `https://github.com/OpenDOP/dop` |
| Licence | Apache-2.0, DCO sign-off |
| Status | Living document. The Markdown file in `docs/architecture/` is authoritative |

## 1 About this document

### 1.1 Purpose

This document defines **what DOP is and how it is built**: its components, its data paths, its storage model, its interfaces and the rules that hold them together. It is the companion of two other documents:

- the **master roadmap** (v1.0, 29.09.2026) says in which order the product is built and what each release must prove;
- the **phase roadmaps** (Phase 1, Revision 1, 05.10.2026 is the first) say how one phase is delivered, package by package.

The architecture reference answers "what"; the roadmaps answer "when" and "how". When a design question comes up during a work package, the answer is looked for here first.

### 1.2 Status of this edition

This is the **second edition**. The first edition (29.09.2026) described the first form of the project. The master roadmap reviewed it and listed 21 architectural gaps, each assigned to a phase. This edition folds every one of those resolutions back into the architecture, so that the architecture and the roadmaps no longer disagree. Appendix A lists the changes.

Two facts about how this edition was produced must be known to its readers:

- It was written **from the master roadmap and the Phase 1 roadmap**, not from the first edition. Section numbers were laid out so that the cross-references the master roadmap makes to the first edition (§14.6, §19.3, §19.4, §20, §21, §22, Figure 2) still point to the right subject.
- Content of the first edition that neither roadmap mentions is **not carried over**. The maintainers compare the two editions once in WP1 and restore whatever is still wanted.

### 1.3 Status markers

DOP is built in vertical slices, so most of this document describes things that do not exist yet. Every section carries one of three markers:

| Marker | Meaning |
|:-------------|:------------------------------------------------------------------|
| **Fixed** | Decided. Backed by an accepted ADR, by §7 of the Phase 1 roadmap, or by a principle of the master roadmap. Changing it needs a new ADR. |
| **Planned** | Direction set by the master roadmap. Exact names, types and parameters are fixed by the roadmap and the ADRs of the phase that builds it. |
| **Open** | A decision that is deliberately not taken yet. It has an owner and a due date. |

Numbers given for later phases (window lengths, thresholds, sample counts) are the master roadmap's defaults or examples. They become facts only when the phase measures them.

### 1.4 Precedence

The project's order of precedence on conflict is: code > accepted ADRs > phase roadmap > master roadmap > discovery reports > conversation. This document is **descriptive**: it does not sit in that chain. Where it disagrees with an accepted ADR or with the code, this document is wrong and is corrected in the same pull request that exposed the difference.

### 1.5 Repository

Source: `https://github.com/OpenDOP/dop` — one public monorepo, licensed under Apache-2.0 with DCO sign-off (ADR-001). This document lives in `docs/architecture/` as Markdown; the Markdown file is the authoritative copy.

Project identity (decision D1.7, closed on 05.10.2026):

| Item | Value |
|:----------------------|:--------------------------------------------------|
| GitHub organisation and repository | `OpenDOP` / `dop` |
| Image registry namespace | `ghcr.io/opendop` — images `dop-core`, `dop-analytics`, `dop-web` |
| Java base package | `io.github.opendop` |
| Protobuf package root | `dop.*` |


## 2 Product vision and positioning

*Status: Fixed (master roadmap).*

### 2.1 What DOP is

DOP — the Distributed Observability Platform — is a **self-hosted, open-source observability product**. It ingests logs, traces and metrics through OpenTelemetry, compresses log noise into patterns, checks the signals with explainable statistical detectors, and correlates what it finds into incidents whose root-cause candidates are ranked with evidence.

The goal is a product that a community really uses: installable by a stranger in ten minutes on a laptop, and operable by an SRE team on a cluster at v1.0.

### 2.2 What makes it different

DOP's difference is **evidence-based incident correlation**. Storing telemetry and drawing it is a solved problem; DOP's value starts where independent anomalies are joined into one incident and every claim about the cause can be clicked through to the log lines, traces, patterns, deployments and metrics that support it.

Three consequences follow, and they are architectural, not marketing:

- **Evidence before AI.** The deterministic pattern, anomaly and correlation engine is the core of the product. A language-model layer comes after v1.0 (§14.6), explains evidence that already exists, and no part of the evidence pipeline depends on it.
- **A ranking, not a verdict.** Root-cause analysis produces ordered candidates with an explainable score. The interface shows, line by line, why a candidate received its score.
- **DOP is not a Grafana.** There is no dashboard editor and no PromQL-like query language. Metrics exist to complete the picture and to serve as incident evidence (§13). DOP works next to an existing Prometheus and Grafana installation and does not try to replace it.

### 2.3 Position among alternatives

The products a user compares DOP with are SigNoz, the Grafana stack, ELK and commercial APM services. They are stronger than DOP in several places — breadth of integrations, dashboards, maturity — and the project says so on an honest comparison page (published with v0.4). DOP competes on one thing: the path from "something is wrong" to "this is the most likely cause, and here is why".

### 2.4 Non-goals

- A dashboard editor, a general query language, alert-rule authoring in the style of Prometheus.
- On-call rotation management and runbook automation.
- A second search database next to ClickHouse.
- A hosted service or a commercial edition before v1.0 (§19.3).
- SAML, SCIM, fine-grained attribute-based access control and a separate database per tenant before v1.0.


## 3 Design principles

*Status: Fixed.*

These principles decide questions that this document does not answer explicitly.

1. **Evidence before AI.** See §2.2.
2. **Vertical slice, not horizontal layer.** Each phase ends with a thin path that works from SDK to browser, never with a finished backend and no interface. Each phase ends in a tagged, installable release.
3. **One path in, one path out.** Telemetry enters through the Collector and the event backbone and nowhere else. Reads leave through the central query builder and nowhere else.
4. **Kafka is the buffer; ClickHouse is the truth.** Core holds no telemetry state that a restart could lose. Offsets move only after storage has acknowledged.
5. **Identity comes from the platform, not from the payload.** Tenant, project and event identifier are assigned by DOP. Nothing a sender writes into an event can change whose data it is.
6. **Schemas change only by migration.** Protobuf through Buf, PostgreSQL through Flyway, ClickHouse through DOP's migration runner.
7. **The laptop is the first production environment.** Defaults are chosen for one machine with 8 GB of memory. Kafka, ClickHouse and PostgreSQL are part of the product, but the single-machine bundle starts them with one command, and a new user sees his own telemetry within ten minutes.
8. **Every claim is explainable.** A detector result carries observed value, baseline, window and detector version. A correlation score is the sum of named, weighted evidence.
9. **Decide, write an ADR, move on.** Every important decision is an Architecture Decision Record in `docs/adr/` (Appendix B).
10. **The simplest working solution.** No abstraction, layer or dependency without a concrete present need.


## 4 System overview

*Status: Fixed for the Phase 1 path; Planned for the rest.*

### 4.1 Context

```
Figure 1 - System context

  applications                                                people
  (OpenTelemetry SDK)                                         (browser)
        |                                                         ^
        | OTLP                                                    | HTTPS
        v                                                         |
  +--------------------------------------------------------------------------+
  |                                   DOP                                    |
  |            ingest -> store -> detect -> correlate -> explain             |
  +--------------------------------------------------------------------------+
        ^                 ^                  |                    ^
        | deploy          | read             | notify             | login
        | webhook         | (optional)       v                    | (optional)
  CI/CD pipeline      Prometheus       Slack, webhook,        OIDC provider
  (Phase 4)           (Phase 6)        e-mail, PagerDuty,     (Phase 5)
                                       Discord (Phase 4)
```

### 4.2 Components and data flow

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

The correction against the first edition: **analytics takes its input from Kafka, asynchronously.** Its only access to ClickHouse is the read of per-minute pre-aggregates that the detectors use as baselines (§14.3). Analytics never writes to ClickHouse or PostgreSQL; its results are events on Kafka that core persists.

### 4.3 Component responsibilities

| Component | Responsibility | State it owns |
|:---------------|:---------------------------------------------|:--------------------------|
| Collector (bundled) | The SDK's OTLP endpoint; batching, memory protection, first redaction layer; writes raw export requests to Kafka | none |
| Event backbone | Buffer between ingestion and storage; transport between core and analytics; home of the dead-letter topics | topics, offsets |
| core | Normalisation and idempotent write; migrations; query API and live tail; incidents, correlation and notifications; identity and scope | none of its own — everything is in Kafka, ClickHouse or PostgreSQL |
| analytics | Pattern mining and anomaly detection | template state, persisted to a compacted Kafka topic |
| ClickHouse | All telemetry and its pre-aggregates | logs, spans, metrics, summaries |
| PostgreSQL | Control plane: projects, data sources, incidents, users, tokens, rules, audit | relational state |
| web | The user interface; a static single-page application | none (filter state lives in the URL) |

### 4.4 What each release adds

| Release | Signal or capability | Architecture sections |
|:---------|:----------------------------------------------|:-----------------|
| v0.1 | Logs end to end: ingest, store, search, live tail | §7–§11 |
| v0.2 | Traces, log-to-trace navigation, service map | §12 |
| v0.3 | Pattern mining, four detectors, Anomaly Center | §14 |
| v0.4 | Incidents, correlation, root-cause ranking, notifications | §15 |
| v0.5 | Users, roles, tokens, OIDC, tenant isolation, audit | §16 |
| v0.6 | Metrics ingest, Metrics Explorer, metric evidence | §13 |
| v1.0 | Helm, backup and upgrade, benchmarks, signed images | §17, §22 |
| v1.x | AI explanation, advanced detectors, ecosystem, governance | §14.6, §19 |


## 5 Repository structure

*Status: Fixed (top level); Planned (contents beyond Phase 1).*

```
/
  contracts/        Protobuf contracts, Buf workspace
  core/             Java 21, Spring Boot, Gradle multi-module
    ingestion/        consumers, normalisation, event identity, write path
    telemetry/        ClickHouse access, schema migrations, retention
    query/            principal-scoped query builder, REST and SSE handlers
    incident/         incident life cycle, correlation, notification
    identity/         principal and scope; users, roles, tokens
    platform/         configuration, health, shared infrastructure
  analytics/        Python 3.12 worker: pattern mining, detectors
  web/              Vite + React single-page application, Nginx image
  infrastructure/   Compose stacks, Collector configuration; Helm chart
  examples/         demo-shop, incident-db-timeout, minimal SDK samples
  scripts/          loadgen and verification scripts
  docs/
    adr/              Architecture Decision Records
    architecture/     this document
    api/              API semantics next to the OpenAPI document
    research/         anonymised user-interview notes
    phase-1/          decision record (D1) and upstream reality report (D2)
    roadmap.md        English summary of the master roadmap
    RUNBOOK.md        local run, release procedure, operations notes
  .github/          workflows, templates, CODEOWNERS
  README.md  CONTRIBUTING.md  SECURITY.md  CODE_OF_CONDUCT.md
  LICENSE  NOTICE  CLAUDE.md  .editorconfig  .gitignore
```

The six core modules are named by the master roadmap. The one-line responsibilities above are the intended split and are confirmed when the core skeleton is built (WP3); a module without content in a phase stays empty and is not filled speculatively.

**Module rule.** A core module speaks to another module through its public service interface, never through its tables or internal classes. One architecture test enforces the dependency direction from the first day.

**Ownership.** Maintainer A ("platform") owns contracts, core, infrastructure and the data stores. Maintainer B ("intelligence and experience") owns analytics, web, CI and the demo scenarios. Documentation, ADRs, releases and community are shared. Ownership means the last word in review, not exclusive access; the incident module is owned jointly on purpose.


## 6 Technology stack

*Status: Fixed. Exact versions are pinned once in WP1 (decision D1.8) and recorded in the upstream reality report; they are not repeated here.*

| Layer | Choice | Decision |
|:--------------|:------------------------------------------------|:-----------|
| Telemetry protocol | OpenTelemetry, OTLP over gRPC and HTTP | master roadmap |
| Entry point | OpenTelemetry Collector, contrib distribution, pinned | master roadmap |
| Event backbone | Kafka API. Redpanda in the Compose bundle; Kafka in KRaft mode as a profile; the user's own Kafka in production | ADR-003 |
| Core language and framework | Java 21, **Spring Boot 4.1.x**, Gradle multi-module; virtual threads on I/O-bound paths | ADR-004 |
| Messaging in core | Spring for Apache Kafka | ADR-003 |
| Control-plane persistence | PostgreSQL with Spring Data JDBC and Flyway. No JPA | ADR-004 |
| Telemetry store | ClickHouse with the official Java client and DOP's own SQL migration runner | ADR-004 |
| Analytics | Python 3.12, uv, ruff, pytest; Drain3 for template mining | master roadmap |
| Contracts | Protobuf with Buf (lint, format, breaking check, code generation for Java and Python) | master roadmap |
| Metrics of DOP itself | Micrometer | master roadmap |
| Tests against dependencies | Testcontainers with real Redpanda, ClickHouse and PostgreSQL; no mocked database | master roadmap |
| Packaging | Docker images for amd64 and arm64 on GHCR; Docker Compose; Helm chart at v1.0 | Phase 1 roadmap |
| CI and release | GitHub Actions, release-please, gitleaks; Trivy, Dependabot or Renovate and SBOM from Phase 5; Cosign from Phase 7 | master roadmap |

**Spring Boot line.** Both roadmaps name "Spring Boot 3.x". The WP1 discovery (05.10.2026) found that open-source support for the whole 3.x line ended on 30.06.2026. The maintainers therefore chose the 4.1.x line on 06.10.2026, with Java 21 unchanged; ADR-004 records the change. The repository was empty, so there was nothing to migrate.

**Frontend (as amended by ADR-002).**

| Concern | Choice |
|:-----------------|:----------------------------------------------------|
| Build and runtime | Vite build, static files served by Nginx. **No Next.js, no server-side rendering, no Node runtime in the bundle** |
| Language and framework | TypeScript in strict mode, React |
| Server state | TanStack Query. No global state library |
| Charts | ECharts |
| Graphs | Cytoscape.js (service map, blast radius) |
| Styling | Tailwind; a small in-repository component set. No third-party UI kit |
| Production-lite proxy | Caddy with automatic TLS (Phase 7) |

## 7 Contracts

*Status: Fixed (conventions, `EventMetadata`, `NormalizedLogEvent`); Planned (later messages).*

Every event that crosses a process boundary is a Protobuf message defined in `contracts/`. There is one source of truth, and Java and Python code are generated from it by the build. Generated code is not committed.

### 7.1 Conventions

- Packages carry a version suffix: `dop.common.v1`, `dop.telemetry.v1`, `dop.analytics.v1`.
- Field numbers are never reused; removed fields are reserved. Enumerations have an explicit zero value.
- Timestamps are `google.protobuf.Timestamp`; identifiers are strings.
- Enumerations for what the code reasons about, plain strings for what senders invent.
- **No algorithm-internal state in a contract.** A contract describes a result (a pattern, an anomaly), never the working memory of the algorithm that produced it.
- Field numbering of `NormalizedLogEvent` is laid out so that the span and metric events of later phases follow the same shape.

### 7.2 Common metadata

`dop.common.v1.EventMetadata` is embedded in every event:

| Field | Meaning |
|:-------------|:--------------------------------------------------------------|
| `event_id` | Identifier assigned by DOP (§8.4). Never taken from the sender |
| `tenant_id`, `project_id` | Assigned by core — from configuration in v0.1, from the ingestion token from v0.5 |
| `environment` | From the deployment-environment resource attribute |
| `occurred_at` | When the event happened at the source |
| `observed_at` | When it was observed by the telemetry pipeline |

### 7.3 Contract catalogue

| Message | Phase | Content |
|:---------------------------------|:-----|:-----------------------------------------|
| `dop.telemetry.v1.NormalizedLogEvent` | 1 | metadata; `service_name`, `service_version`; `severity_number`, `severity_text`; `body`; `attributes` and `resource_attributes` as string maps; `trace_id`, `span_id` |
| `dop.telemetry.v1.NormalizedSpanEvent` | 2 | metadata; trace, span and parent identifiers; service, name, kind; start and end; status; attributes; resource |
| `ServiceEdgeObserved` (internal) | 2 | caller, callee, window, call count, error count, latency summaries |
| `dop.analytics.v1.LogPatternDetectedEvent` | 3 | stable `pattern_id`, template, first and last seen, count in window |
| `dop.analytics.v1.AnomalyDetectedEvent` | 3 | `detector_id`, `detector_version`; scope (service, pattern or endpoint); observed, baseline, window, score; evidence references |
| `DeploymentObserved` | 4 | service, old and new version, source (observed or reported) |
| `dop.telemetry.v1.NormalizedMetricPoint` | 6 | metric name; type (gauge, sum, histogram); temporality flag; attributes; resource; value or buckets; exemplar `trace_id` |

### 7.4 Compatibility

- `buf lint` and `buf format` run on every change from the first week. A cross-language golden test proves that a message written by Java is read by Python and the reverse.
- The **Buf breaking check** becomes a required gate when v0.1.0 is tagged, with v0.1.0 as its baseline.
- From Phase 3 the contracts are backward-compatible commitments: a field is removed only after a deprecation period and only in a major version.
- A change in `contracts/` runs the Java and the Python consumers together in CI.


## 8 Ingestion

*Status: Fixed for logs (Phase 1 roadmap §7); Planned for spans, metrics, tokens and quotas.*

### 8.1 The Collector is the entry point

The bundled OpenTelemetry Collector is the target of the user's SDK: OTLP over gRPC on port 4317 and over HTTP on port 4318. The Compose file, the README and the quickstart all name the same two addresses. Its log pipeline is:

```
otlp receiver -> memory_limiter -> batch -> redaction (sample rule)
              -> kafka exporter (otlp_proto encoding)
```

- **One Kafka message is one serialised OTLP export request** and holds many resources, scopes and records. The unit of consumption is therefore a batch, and a single bad message can carry hundreds of healthy records.
- The batch processor's maximum batch size, the exporter's message limit and the broker's message limit are three numbers chosen together, with margin, and documented next to each other.
- The sample redaction rule masks card-number-like values. It is an example the operator extends, not a privacy guarantee.
- A user who already runs a Collector may point it at DOP's Kafka instead of using the bundled one.
- The Collector's ClickHouse exporter is not used: it would bypass the buffer, tenant assignment and the schema DOP owns.

### 8.2 Topics

| Topic | Content | Notes |
|:---------------------|:--------------------------------|:---------------------------|
| `dop.otlp.logs.v1` | Raw OTLP log export requests written by the Collector | Short retention: a buffer, not an archive |
| `dop.otlp.logs.dlq.v1` | Messages core could not process, with reason, original coordinates and timestamp as headers | Longer retention |
| raw span and metric topics | Same pattern as logs | Phases 2 and 6; named by those phases |
| normalised log topic | `NormalizedLogEvent`, **partition key `hash(tenant_id, service_name)`** | Phase 3; all logs of one service reach the same analytics worker |
| analytics result topics | `LogPatternDetectedEvent`, `AnomalyDetectedEvent` | Phase 3; consumed and persisted by core |
| pattern-state topic | Template-miner state per (tenant, service), compacted | Phase 3 (§14.2) |

Topic names follow `dop.<domain>.<subject>.v<n>`. Topics are created by an idempotent init step at start; partition counts and retention for the laptop profile are fixed by the package that creates the topic.

### 8.3 The consumer in core

```
poll -> decode export request -> for each resource / scope / record:
          normalise (pure function) -> assign tenant, project, event_id
     -> buffer -> batched INSERT into ClickHouse -> commit offsets
```

- **Normalisation is a pure function** from (resource, scope, record, message coordinates) to a normalised event, with no storage dependency. Publishing its output to the normalised topic in Phase 3 is therefore an addition, not a rewrite.
- Normalisation rules for logs: service name and version from resource attributes (a missing service name becomes `unknown_service`); `occurred_at` from the record's time, falling back to its observed time; attribute values stored as strings — numbers and booleans rendered canonically, arrays and maps as JSON; the OTLP severity number kept as is, with six display levels (TRACE, DEBUG, INFO, WARN, ERROR, FATAL) derived from its ranges and UNSET for unspecified; trace and span identifiers as lower-case hexadecimal, empty when absent.
- Rows are buffered and inserted when a row count or a wait time is reached. **Offsets are committed only after ClickHouse has acknowledged the insert.** ClickHouse asynchronous inserts are not used, because they blur that acknowledgement.
- Graceful shutdown: stop polling, flush the buffer, commit, close.

### 8.4 Event identity and the idempotent write (ADR-015)

An OTLP log record carries no identifier of its own, so DOP constructs one.

- `event_id` is a **deterministic value computed from topic, partition, offset and the record's ordinal within the decoded message.** Reprocessing a message yields the same identifiers; two different records never share one.
- The telemetry table is a `ReplacingMergeTree` whose ordering key ends in `event_id`. Rows with an identical key collapse at merge time; the read path collapses those not yet merged.
- **Guaranteed:** a Kafka message consumed twice (restart, rebalance, crash between insert and commit) produces one visible row per record.
- **Not guaranteed:** a batch the Collector itself writes to Kafka twice arrives as two messages with different offsets and is stored twice. The exporter's delivery settings keep this rare, and the documentation names it as a limitation.
- Rejected: a content hash (two identical lines with the same timestamp are legitimate and would be deleted silently); a random UUID at consumption (not idempotent); Kafka transactions (ClickHouse is not a participant).

### 8.5 Failure handling

Two kinds of failure, never mixed:

| Failure | Behaviour |
|:-----------------------|:-------------------------------------------------------|
| A message cannot be decoded | Sent to the dead-letter topic at once, with its reason; the consumer moves on. Never guessed, never silently dropped |
| Storage is unavailable | The write is retried with bounded back-off while the consumer pauses. Kafka is the buffer and consumer lag is the visible symptom. **A healthy message is never dead-lettered because ClickHouse was down** |
| ClickHouse rejects a batch as data | Dead-lettered after the bounded retries |

The dead-letter count is visible in the metrics and on the Overview's health panel. In v0.1 the dead-letter topic is inspected with the broker's own tooling; a replay tool arrives in Phase 7.

### 8.6 Later additions to the ingestion path

- **Sampling (Phase 2).** Tail sampling needs all spans of a trace on one Collector. When Collectors are scaled horizontally, a two-tier topology is used: a first tier with the load-balancing exporter routes by trace to a second tier of sampling Collectors. A Compose profile and a guide ship with v0.2. This resolves the first edition's contradiction between recommending tail sampling and scaling the Collector.
- **Ingestion tokens (Phase 5).** Tenant and project are derived from an ingestion token carried as a header through the Collector and verified in core. A value inside the event is overwritten. Telemetry without a token is refused.
- **Quotas (Phase 5).** A token-bucket rate limiter per tenant in core.
- **Second redaction layer (Phase 5).** A configurable attribute drop and hash list in core, behind the Collector's rules.


## 9 Storage

*Status: Fixed for the logs table and both migration paths; Planned for later tables.*

### 9.1 Rules that hold for every table

- No table is created outside a migration. Migrations are forward only and carry checksums; a changed checksum of an applied file fails the start.
- `tenant_id` and `project_id` are on every telemetry row, and **`tenant_id` is the first element of every ordering key** (ADR-009).
- All timestamps are stored in UTC; telemetry time has nanosecond precision. No floating point for counts. Identifiers are UUIDs.
- Runtime database users hold the smallest privileges the service needs; schema changes run only through the migration paths.
- No real telemetry in any fixture.

### 9.2 ClickHouse — telemetry

**Migration runner.** Numbered SQL files, a `schema_migrations` table with checksums, applied at core start under a lock, so that two instances starting together apply each migration once.

**The logs table (v0.1).**

| Aspect | Design |
|:------------------|:------------------------------------------------------------|
| Engine | `ReplacingMergeTree` |
| Partition | One per day of `timestamp`; whole partitions leave when their TTL passes |
| Ordering key | `(tenant_id, service_name, timestamp, event_id)` |
| Identity columns | `tenant_id`, `project_id`, `environment`, `event_id` |
| Time columns | `timestamp` (occurred), `observed_at`, `ingested_at` (set by ClickHouse at insert; the live-tail cursor) |
| Record columns | `service_name`, `service_version`, `severity_number`, `severity_text`, `body`, `trace_id`, `span_id` |
| Attributes | `attributes` and `resource_attributes` as `Map(String, String)`; materialised columns for a short list of frequent keys (environment, host, Kubernetes namespace and pod), extended only by migration |
| Indexes | Token bloom filter on the lower-cased body; bloom filter on `trace_id` |
| Retention | Row TTL on `timestamp` from one setting (7 days by default in Compose); a small job at start aligns the table with the setting |

Exact types, codecs and index parameters are the output of a benchmark on ten million rows and live in the migration file, which is authoritative.

**Full-text search without a second database.** The token index answers token questions, not substring questions. Free-text search is therefore defined exactly: the query is split into tokens with the tokeniser the index uses, lower-cased, and all tokens must be present. The index prunes; the predicate decides. Substring and regular-expression search are not offered in v0.1, and the interface says so.

**Later tables.**

| Table | Phase | Design notes |
|:--------------------|:----|:--------------------------------------------------|
| `spans` | 2 | Ordering key `(tenant_id, trace_id, start_time)`. Service-based search may need a projection or a second table; decided by benchmark (§12) |
| `trace_summary` | 2 | Root service, total duration, error flag, span count, service set, "late" flag |
| per-minute pre-aggregates | 3 | Materialised views: counts by service and severity, counts by pattern, span latency summaries. The detectors' baselines |
| metric tables | 6 | Gauge and sum in one table, histogram buckets in another (§13) |

Log rows are enriched with `pattern_id` from Phase 3. Counts shown on the Overview are read without de-duplication and are documented as approximate; search results are exact.

### 9.3 PostgreSQL — control plane

Flyway migrations; repositories with Spring Data JDBC; aggregate-oriented, no lazy loading.

| Phase | Tables |
|:------|:-----------------------------------------------------------------------|
| 1 | `projects` (identifier, tenant identifier, name, timestamps); `data_sources` (identifier, project, kind, name, timestamps). The default tenant, project and Collector data source are seeded |
| 3 | Patterns and anomalies, detector configuration |
| 4 | Incidents and their timeline, evidence and candidate feedback; deployments; notification channels, routing rules, mute windows, delivery history |
| 5 | Organisations, users, roles, API tokens, ingestion tokens (attached to data sources), audit log |

Which parts of patterns and anomalies live in PostgreSQL and which in ClickHouse is fixed by Phase 3.

### 9.4 Tenant isolation in storage (ADR-009, Phase 5)

One set of tables for all tenants. Isolation is enforced by the application: one central query builder forces every query into a tenant and project scope, and a query without a scope fails the build (§10.2). A separate ClickHouse database per tenant is not offered before v1.0. Retention becomes a per-tenant setting in Phase 5.

### 9.5 Deletion and backup

- **Deletion (Phase 5).** A job deletes by tenant, project or attribute using ClickHouse lightweight deletes. It is asynchronous and has a cost; both are documented.
- **Backup and restore (Phase 7).** ClickHouse `BACKUP`; PostgreSQL dump and point-in-time recovery; runbooks tested by restoring into a clean cluster.


## 10 Query and API

*Status: Fixed (principal, scope, query builder, conventions, v0.1 endpoints); Planned (later endpoints).*

### 10.1 Principal and scope (ADR-016)

- A **principal** is a value object: who is asking, for which tenant and project, with which permissions. One component resolves it per request. Handlers receive it as a parameter; they never construct it and never read tenant or project from a request parameter.
- A **scope** is derived from the principal and is what a query is built from.
- In v0.1 the resolver always returns one built-in principal for the default tenant and project. **Phase 5 replaces the resolver, not the handlers.** Building this for one tenant costs a few classes; retrofitting it would touch every endpoint and every query.

### 10.2 Central query builder

The only code that produces ClickHouse SQL for reads.

- It cannot be invoked without a scope; there is no overload without one. Tenant and project predicates are added by the builder, not by the caller.
- Every user-supplied value is a bound parameter. Attribute keys are validated against a fixed pattern before use. No query is assembled by concatenating input.
- A test fails the build if SQL for a telemetry table is constructed anywhere else.

### 10.3 API conventions

- REST under `/api/v1`. The **OpenAPI document is the contract**; `docs/api/` explains semantics. `/api/v1` is frozen at v1.0; before that, a change is listed under "Breaking" in the release notes.
- Errors are `application/problem+json` with a stable code — among them `VALIDATION_FAILED`, `RANGE_TOO_LARGE`, `QUERY_TIMEOUT`, `TAIL_LIMIT_REACHED`, `STORAGE_UNAVAILABLE`. "Storage unavailable" is always distinct from an empty result.
- **Keyset pagination** with an opaque cursor over (timestamp, event identifier). No offset pagination anywhere.
- Server-side limits — maximum time range, page size, query timeout, concurrent tails — are settings. A request that exceeds one is **refused with a specific code, not truncated silently**.
- New endpoint = principal + scope + server-side limits by default.

### 10.4 Endpoint catalogue

| Release | Endpoints |
|:-------|:----------------------------------------------------------------------|
| v0.1 | `GET /logs/search`, `GET /logs/tail` (SSE), `GET /services`, `GET /overview/ingest`, `GET /system/health`, `GET /meta` |
| v0.2 | `/traces/search`, `/traces/{id}`, `/service-map`, RED summaries per service |
| v0.3 | `/patterns`, `/anomalies`, detector configuration |
| v0.4 | Incidents (list, detail, acknowledge, resolve, candidate feedback), `POST /deployments`, notification channels and rules |
| v0.5 | Login and session, users and roles, projects and environments, tokens, audit |
| v0.6 | `/metrics/query`, `/metrics/catalog` |

Paths are relative to `/api/v1`. Management endpoints (`/actuator/health`, readiness, metrics) are on a separate port that is not published outside the Compose network.

### 10.5 Live tail

The tail is **a read that repeats**, not a second pipeline. Each server-sent-events connection keeps a cursor on (ingestion time, event identifier) and asks the query builder for newer rows at a fixed interval, behind a small safety lag so that concurrent inserts are not skipped.

- Every event carries an identifier; a reconnect with `Last-Event-ID` resumes after it. A heartbeat keeps intermediaries from closing an idle stream.
- When a filter matches more than the stream can deliver, the stream says so with a "rows skipped" event.
- It needs no extra topic, shares every filter with search, works unchanged with several core replicas, and its cost is bounded by a connection limit.
- Rejected: a push from the consumer's memory (one instance only, bypasses scope); a Kafka consumer per connection; WebSockets (the data flows one way).


## 11 Web application

*Status: Fixed (foundation and v0.1 pages); Planned (later pages).*

### 11.1 Foundation

- A static single-page application. Nginx serves it with history fallback and proxies `/api/` to core, so the browser talks to one origin.
- Application shell with navigation, routing and a global time-range control. **The whole view state — time range and filters — lives in the URL**, so a view is shared as a link.
- A typed API layer generated from or checked against the OpenAPI document, with runtime validation at the boundary: a renamed backend field fails loudly instead of rendering an empty cell.
- Distinct states for empty, loading, refused by a limit, and backend unavailable.

### 11.2 Security properties of the interface

- **Telemetry text is always rendered as text.** No HTML from a log body or an attribute reaches the DOM.
- The Content-Security-Policy allows the application's own origin only. Fonts, icons and scripts are bundled; the application makes no request to a third-party origin.
- In v0.1 a persistent banner states that there is no authentication and the port must not be exposed.

### 11.3 Pages by release

| Release | Pages |
|:-------|:----------------------------------------------------------------------|
| v0.1 | **Overview** (services, ingest per minute, system health); **Log Explorer** (filters, virtualised result table, detail drawer, live tail) |
| v0.2 | **Trace Explorer**, waterfall view, **Service Map**; log → trace and span → logs navigation |
| v0.3 | **Pattern view** (templates, sparklines, drill-down, "new" badge); **Anomaly Center** (observed against baseline); detector settings |
| v0.4 | Incident list; **Incident Workspace** (timeline, blast radius, evidence tabs, ranked candidates with their explanation, acknowledge and resolve, candidate feedback); notification settings |
| v0.5 | Login; users and roles; projects and environments; tokens; audit log; tenant and project selector |
| v0.6 | **Metrics Explorer** (structured query builder); service page with RED, runtime and host metrics side by side |
| v1.0 | First-run wizard, empty states, user-friendly error messages |

Usable and fast is the bar. A dashboard editor is never built.


## 12 Traces and service map

*Status: Planned (Phase 2, v0.2). Semantics recorded in ADR-010.*

### 12.1 Span pipeline

Spans follow the log path: the Collector writes raw OTLP to Kafka, a span consumer in core normalises each span into a `NormalizedSpanEvent` and writes it to the `spans` table with the same identity, retry and dead-letter rules as logs (§8).

### 12.2 Trace assembly

The first edition did not say how a trace is assembled or what happens to late spans. The rule is:

- **Spans are not collected in memory. Each span is written on its own.**
- "The trace is complete" is decided after the root span has been seen and a configurable late-arrival window has passed (`trace.completion.window`, default 60 seconds).
- The `trace_summary` row is updated when the window closes, by a materialised view and a periodic job.
- A span that arrives after the window is still stored, and the summary is recomputed with a "late" flag.

### 12.3 Ordering key

`(tenant_id, trace_id, start_time)` serves trace-by-identifier. Service-based search needs a different order; whether a projection, a materialised view or a second table provides it is decided by benchmark in Phase 2. A two-table solution is acceptable.

### 12.4 Service edges and RED

- Service edges are derived from client and server span pairs, aggregated in one-minute windows (`ServiceEdgeObserved`): caller, callee, call count, error count, latency summaries.
- The **service map** is drawn from those edges: node colour from error rate and latency, edge thickness from traffic.
- **RED summaries per service — rate, errors, duration — are produced from spans.** They do not depend on the metrics pipeline, which arrives four phases later.

### 12.5 Links between signals

`trace_id` and `span_id` are stored, indexed and searchable on log rows from v0.1. Phase 2 therefore adds navigation — from a log line to its trace, from a span to its logs — and not a migration. From Phase 2 core and analytics also send their own traces to DOP.


## 13 Metrics

*Status: Open — ADR-005, decision due at the start of Phase 6. What follows is the master roadmap's proposal.*

### 13.1 Strategy

| Option | Consequence |
|:--------------------|:------------------------------------------------------------|
| A — native only | OTel metrics stored in ClickHouse; DOP is self-sufficient, but teams with a Prometheus investment collect twice |
| B — read from Prometheus only | DOP stores no metrics; an extra dependency for single-machine and air-gapped users |
| **Proposal — A plus a thin B** | Native ingest is part of v1.0. Prometheus is an optional "external metrics source", read only for incident evidence and side-by-side display |

Metrics are deliberately late: DOP's difference is not a metric dashboard, and the ecosystem is strong there. They are finished before v1.0 because a platform without them reads as incomplete. Feedback from v0.1–v0.5 users is collected before the decision.

### 13.2 Design under the proposal

- Contract: `NormalizedMetricPoint` (§7.3).
- Storage: tables by type — gauge and sum together, histogram buckets separately.
- **Temporality is normalised in the query layer**: cumulative series are turned into rates with tolerance for counter resets, so that two SDKs sending delta and cumulative produce the same chart.
- **Cardinality protection (ADR-013)**: attribute allow and deny lists, a limit on series per metric; above the limit, ingest continues, the excess series are dropped, and a warning metric and a visible warning are raised.
- Exemplars carry a `trace_id`, linking a metric point to a trace.
- Prometheus adapter: configured queries are pulled from a Prometheus endpoint and attached to incidents as evidence.
- The existing detectors are reused on metric series; "metric anomaly" becomes an evidence type in correlation (§15.2).

### 13.3 Boundaries

No PromQL-like language; a structured query builder in the interface is enough. No dashboard editor; fixed service and infrastructure panels plus the Explorer. Exposing DOP as a Grafana data source is a candidate for v1.x.

## 14 Analytics and intelligence

*Status: Planned (Phase 3, v0.3). §14.6 is after v1.0.*

This is where DOP stops being only a telemetry store: noise is compressed into patterns and deviations are marked by detectors a person can check by hand.

### 14.1 Worker model

- The analytics worker is a Python process that consumes **normalised log events from Kafka**. It does not read raw OTLP and it does not read logs from ClickHouse.
- The normalised log topic is keyed by `hash(tenant_id, service_name)`. All logs of one service reach the same worker, so that template state stays consistent; a large tenant is still spread over several partitions.
- Results leave as `LogPatternDetectedEvent` and `AnomalyDetectedEvent` on Kafka. Core persists them, enriches log rows with `pattern_id` and serves them through the API.
- In v0.1 and v0.2 the worker is built, tested and published but not started by the quickstart, because it has no function yet.

### 14.2 Pattern mining

- Online template mining based on **Drain3**.
- **State.** The miner's state is kept per (tenant, service) and persisted to a compacted Kafka topic, using Drain3's built-in Kafka persistence. On a partition rebalance the worker loads the state of the partitions it was assigned.
- **Stable pattern identity.** `pattern_id = hash(tenant, service, normalised template)`. Drain's internal cluster identifier never leaves the worker. A restart or a change of replica does not change an identifier, and two replicas never produce two patterns for the same service.
- Drain3 parameters (similarity threshold, depth) depend on log style. Defaults are tuned on the demo; a per-service override is offered.

### 14.3 Baselines and warm-up

- **Baselines are not held in memory.** In every window the detectors read per-minute pre-aggregates from ClickHouse — counts by service and severity, counts by pattern, span latency summaries. Those pre-aggregates are maintained by materialised views on the core side. This read is the single legitimate analytics → ClickHouse access (Figure 2).
- **Warm-up.** A new service or pattern needs a minimum number of samples and a minimum time (for example 24 hours or 200 windows). Until then the detector publishes the state "learning", raises no alarm, and the interface shows "learning".

### 14.4 Detectors

| Detector | Signal | Method |
|:------------------|:----------------------------|:--------------------------------------|
| Frequency spike | Count of a pattern per window | Robust z-score on a rolling median and MAD |
| New pattern | A template not seen before for the service | First occurrence after warm-up |
| Error-rate spike | Share of error-level records per service | Deviation from the rolling baseline |
| Latency spike | Span latency summaries | Deviation from the rolling baseline |

- Every result carries **observed value, baseline, window and detector version** as evidence, and references to the logs and traces behind it.
- The detector interface is extensible and is the project's first contribution surface for the community. Seasonal baselines (daily, weekly), clustering-based new-behaviour detection and endpoint-level latency comparison are v1.2 work; the plugin interface is stabilised then.
- Metric-based detectors reuse the same code on metric series in Phase 6.
- False positives decide whether the product is trusted. The last two weeks of Phase 3 are reserved for tuning and for measuring the false-positive rate.

### 14.5 Throughput and the fallback plan (ADR-006)

In v0.3 all normalised logs go to the Python worker and throughput is measured. If one worker instance stays below **3,000 logs per second on a laptop**, or lag accumulates for a single service, template mining moves to Java in Phase 7 (a Java port of the Drain algorithm) and Python keeps the detectors and advanced analysis. The ADR is written in Phase 3; the measurement closes it.

### 14.6 AI explanation layer (after v1.0, target v1.1)

- Two adapters, kept apart: a **local model** adapter (Ollama or any OpenAI-compatible endpoint) and an **external provider** adapter.
- Sending telemetry outside the installation is an **explicit opt-in**.
- The prompt is built only from the evidence of an incident. The output is labelled "generated by AI" and shown with links to the evidence it was built from.
- **No part of the evidence pipeline depends on a language model.** With the layer switched off, every detector, correlation and ranking works exactly as before.


## 15 Incidents, correlation and alerting

*Status: Planned (Phase 4, v0.4). Rules recorded in ADR-007, ADR-008, ADR-011 and ADR-012.*

### 15.1 Incident life cycle (ADR-007)

State machine: `open → acknowledged → resolved → closed`.

| Rule | Definition |
|:-------------|:------------------------------------------------------------------|
| Opening | One anomaly above the severity threshold, **or** at least two anomalies that are connected (same trace, a service-graph edge, or the same time window) |
| Merging | A new anomaly joins an open incident when it intersects the incident's affected services on the service graph and falls inside its time window |
| Closing | No new evidence during a configurable cool-down → automatically "resolved"; "closed" on user confirmation |
| Flapping | Hysteresis: the closing threshold is lower than the opening threshold; an incident that reopens shortly after closing is the same record reopened |
| De-duplication | A deterministic key: tenant, root service set, dominant pattern |

### 15.2 Correlation engine (ADR-011)

The engine runs in core. It joins anomalies through typed evidence:

- co-occurrence in the same trace;
- an edge of the service graph;
- temporal precedence, measured on `occurred_at` with a window that tolerates clock skew;
- a shared downstream dependency;
- a deployment event;
- a simultaneous change of patterns;
- a metric anomaly (from Phase 6).

Each evidence type has an **explicit weight**, and a candidate's score is explainable: the interface shows line by line why a candidate received, say, 0.82. Weights will not be right the first time; the goal is not a perfect ranking but that every claim's evidence can be clicked. A user can confirm or reject a candidate, and that feedback is stored for later calibration.

### 15.3 Coordination between core replicas (ADR-008)

Opening, merging and closing incidents must not run twice. These operations run in a single **orchestrator role**, given to one replica through a PostgreSQL advisory lock. The other replicas continue with ingestion and queries. The alternative — binding the anomaly topic to one partition per tenant — is documented and not chosen, because the lock is simpler.

### 15.4 Deployment context

Two sources feed the incident timeline and the correlation evidence:

1. Core observes that a service's `service.version` resource attribute has changed and emits `DeploymentObserved`.
2. A CI/CD pipeline reports a deployment through `POST /api/v1/deployments` (a GitHub Actions example is provided).

Kubernetes events (rollouts, OOMKilled) as deployment context are v1.x work.

### 15.5 Alerting and notification (ADR-012)

- Channels: generic webhook, Slack (incoming webhook), e-mail (SMTP), PagerDuty (Events API v2), Discord. Webhook and Slack come first.
- Routing rules per project by severity and service; message templates; mute windows.
- Delivery is retried and every attempt is kept in a delivery history.
- A notification carries a deep link to the incident.

There is no alert-rule language: a notification is the consequence of an incident, not of a threshold a user wrote.


## 16 Multi-tenancy, identity and security

*Status: Fixed (v0.1 posture, principal and scope); Planned (Phase 5, v0.5; ADR-009).*

### 16.1 Posture of v0.1 to v0.4

These releases have **no authentication, and say so**. The architecture's job is to not make that worse and to leave no debt for Phase 5:

- The default Compose profile publishes the interface, the API and the OTLP ports on `127.0.0.1` only, and publishes no data-store port at all. Opening them is an explicit setting with a documented warning. This is a safety default, not a security feature.
- README, quickstart, release notes and a banner in the interface state that the release is unauthenticated.
- Principal, scope and the central query builder exist from the first endpoint (§10).
- Tenant, project and event identifier are assigned by core and cannot be set by a sender.

### 16.2 Identity and access (Phase 5)

- Hierarchy: **organisation → project → environment**; permissions are scoped at project level.
- Local user accounts with argon2 password hashing; session or JWT.
- Roles: **owner, admin, operator, viewer**.
- API tokens: stored hashed, time-limited, with the smallest scope; shown once.
- Ingestion tokens: the source of tenant and project identity on the write path (§8.6).
- OIDC adapter, tested against Keycloak. SAML and SCIM are after v1.0.

### 16.3 Isolation

- Storage: one table set, `tenant_id` first in every key, scope forced by the query builder (§9.4).
- Kafka: the partition key `hash(tenant, service)` spreads a large tenant over several partitions. The remaining **hot-partition risk** — one very loud service of one tenant — is documented and measured in the Phase 7 benchmark.
- Quotas: a per-tenant ingest quota enforced by a token-bucket limiter in core; per-tenant retention.
- Proof: a two-tenant integration test in which no API, SSE or interface path of tenant A can return data of tenant B.

### 16.4 Privacy

- **Two redaction layers.** Ready-made Collector templates (authorization headers, e-mail addresses, card numbers, SQL literals) and, behind them, a configurable attribute drop and hash list in core.
- **Right to erasure.** A deletion job by tenant, project or attribute (§9.5).
- Core never writes a full telemetry payload, a dead-letter payload or a connection string to its own log.

### 16.5 Audit, secrets and supply chain

- An audit-log table and view for critical administrative actions (Phase 5).
- Secrets come only from environment or configuration; an `.env.example` is provided; from Phase 5 Compose refuses to start with weak default passwords.
- All containers run as non-root users; base images and upstream components are pinned to exact versions.
- Secret scanning with gitleaks before commit and in CI from Phase 1. Dependency and image scanning with Trivy, Dependabot or Renovate, and a CycloneDX SBOM as a release artefact from Phase 5. Images signed with Cosign and a written threat model in Phase 7.
- Vulnerabilities are reported through GitHub private vulnerability reporting (`SECURITY.md`).


## 17 Deployment and operations

*Status: Fixed (Compose bundle of v0.1); Planned (Kubernetes and operations, Phase 7).*

### 17.1 Compose bundle

One stack in `infrastructure/`, with a health check and a named volume for every stateful service and start order by health, not by sleep.

| Mode or profile | Purpose |
|:------------------|:------------------------------------------------------------|
| quickstart | Pulls released images. The user downloads one Compose file and runs one command; nothing is cloned or built |
| development override | Builds from source; publishes data-store ports on the loopback interface |
| `kafka` | Kafka in KRaft mode instead of Redpanda |
| `analytics` | Starts the analytics worker (needed from v0.3) |
| `demo` | Starts the demo shop and its traffic generator |
| tail-sampling | Two-tier Collector topology (v0.2) |
| production-lite | Caddy with automatic TLS (v1.0) |

**Memory budget.** The quickstart profile must run on a machine with 8 GB of RAM next to a browser and an IDE. The budget is written down and enforced by container limits: Redpanda in its single-core development mode, ClickHouse with a lowered server memory ceiling, a fixed JVM heap for core.

**The ten-minute rule.** On a clean machine with no cached images, a new user sees his own telemetry in the interface within ten minutes. The time is measured with a stopwatch on an Intel and an ARM laptop at every release and written into the release notes.

### 17.2 Kubernetes (Phase 7)

A Helm chart for core, analytics, web and the Collector. Kafka, ClickHouse and PostgreSQL are either bundled or external. Liveness, readiness and start-up probes reflect the state of dependencies. Autoscaling examples: core on consumer lag, analytics on partitions. PodDisruptionBudgets, NetworkPolicy examples, ingress with TLS. The chart is kept minimal and documented; "every environment" is not a goal.

### 17.3 Operations (Phase 7)

Backup and restore runbooks; a tested upgrade path between releases (Flyway and ClickHouse migrations, Kafka topic compatibility); a dead-letter inspection and replay command-line tool; graceful shutdown with offset-commit guarantees; tuning guides for ClickHouse and Kafka; a capacity calculator (§22).

### 17.4 Configuration

Settings are typed, live under one `dop.*` prefix and fail the start loudly when unknown or invalid. The Phase 1 roadmap (§12) holds the catalogue for v0.1: default tenant and project, topic names, batch size and wait, retry bounds, log retention, query limits, tail behaviour, and the Compose bind address.

### 17.5 DOP observes itself

- From v0.1, Micrometer metrics: records written and dead-lettered, batch size and duration, consumer lag per partition, write retries, ClickHouse query duration (p95 tracked), open tail streams and skipped rows, API latency and error rate. They surface on the Overview's health panel.
- From v0.2, core and analytics send their own traces to DOP — "DOP monitoring DOP" is also the best demo.
- From v0.3, detector delay is measured.
- At v1.0, a Prometheus scrape endpoint and a ready-made Grafana dashboard.


## 18 Quality, CI and release

*Status: Fixed.*

### 18.1 Tests

- **Pyramid.** Fast unit tests; integration tests against real Redpanda, ClickHouse and PostgreSQL through Testcontainers; an end-to-end smoke test in which the Compose stack starts, demo telemetry flows and the API and one browser test confirm it.
- **Regression pack.** Every acceptance criterion of every phase that can be automated is a named test or script. At each phase close, the criteria of all earlier phases are run again; the pack only grows.
- **Rules.** Security and acceptance claims are proven with raw output, not with test names. A failing end-to-end run is diagnosed, never re-run until green.

### 18.2 CI

Changed-path aware: a change in `contracts/` runs the Java and the Python jobs together, other paths run only their own module. Buf lint and format, tests per module, container builds, secret scanning, a DCO check and a Conventional Commits check on every pull request. A nightly run executes the full integration suite and a short load test and opens an issue on regression.

### 18.3 Release

- **SemVer.** Before v1.0, a minor version is a phase closing. After v1.0, monthly minors, patches when needed, breaking changes only in a major.
- **Automation.** Conventional Commits and release-please produce changelog, tag and GitHub Release; the tag workflow builds the images for both architectures and pushes them to GHCR.
- **Compatibility.** Contracts as in §7.4; `/api/v1` frozen at v1.0; storage migrations forward and automatic, rollback by runbook.
- **Supported versions.** After v1.0, security patches for the last two minors (ADR-014).
- **Release notes.** Highlights, Breaking, Upgrade notes, Contributors — and a screenshot or GIF.


## 19 Project model

*Status: Fixed (§19.1, §19.4); Planned (§19.2, §19.3).*

### 19.1 Open source

- **Licence: Apache-2.0** (ADR-001) — compatible with the CNCF ecosystem, with a patent clause, recognised by corporate legal departments. Contributions are signed off under the **DCO**; there is no CLA.
- The repository is public from the first commit. There is no private history to clean later; licence, contribution rules and secret scanning exist before the first feature commit.
- Repository language: English for code, documentation, issues and ADRs.

### 19.2 Community and governance

- GitHub Discussions is the permanent, searchable channel; Discord for live help from Phase 2; a monthly community call from Phase 5.
- Launch moments: v0.1 (quiet), v0.4 (first large launch), v1.0 (main launch).
- After v1.0: a public RFC process, community votes on roadmap priorities, a documented way to become a maintainer, and the goal of raising the bus factor from two to four.

### 19.3 Commercial exploration (after v1.0)

Possible directions are support contracts, managed hosting and premium integrations. One rule is fixed now: **the open-source core keeps its value — no v1.x feature is held back for a commercial offering.**

### 19.4 AI coding assistants

AI coding assistants are used throughout the project, under rules the repository enforces:

- The diff and the test output are always read by a human. Contract code and security-relevant code are reviewed by both maintainers.
- The assistant never commits or pushes; every commit is made and signed off by a person.
- Work proceeds in rounds: a read-only discovery first, one prompt at a time, and every round reports the assumptions that turned out wrong and any overengineering it found.
- The standing rules live in `CLAUDE.md` at the repository root.


## 20 Target users

*Status: Fixed (master roadmap).*

| User | What brings them | From |
|:------------------|:-----------------------------------------------|:------|
| Individual developers and small teams | Logs with the lowest barrier to entry; one command; no operations knowledge about Kafka or ClickHouse needed | v0.1 |
| Teams running distributed services | Traces, service map, and from v0.4 the incident view that joins the signals | v0.2–v0.4 |
| Research and education users | An open, explainable implementation of log pattern mining and statistical anomaly detection that can be studied and extended through the detector interface | v0.3 |
| Organisations in regulated, on-premises or air-gapped environments | Self-hosted by design, tenant isolation, audit, no data leaving the installation | v0.5 |
| Teams with an existing Prometheus and Grafana | DOP next to their stack: Prometheus as an evidence source, no forced migration | v0.6 |
| SRE and platform teams | Helm chart, backup and upgrade paths, published benchmarks | v1.0 |

The assumed user of v0.1 runs DOP on a single machine he controls and knows what OpenTelemetry is, or can follow a guide.


## 21 Reference scenario: database timeout

*Status: Planned. The scenario is the acceptance test of Phases 3, 4 and 6 and the content of the v0.4 launch demo.*

### 21.1 Topology

```
load generator -> gateway -> checkout -> payment -> PostgreSQL
```

The demo shop starts in v0.1 with three services in Java, Python and Node, instrumented with OpenTelemetry automatic instrumentation only. Phase 2 extends it to the four-hop chain above with working context propagation. `examples/incident-db-timeout` reproduces the fault with one command by saturating the PostgreSQL connection pool.

### 21.2 What DOP is expected to show

| Step | Expected behaviour | Section |
|:-----|:----------------------------------------------------------------|:------|
| 1 | The fault is injected: the connection pool of the database is saturated | — |
| 2 | Payment logs a timeout line it has never logged before; error lines rise in payment, then checkout, then gateway; span latency rises along the chain | §8, §12 |
| 3 | Within 2 minutes the Anomaly Center shows a **new pattern** and an **error-rate spike**, each with observed value and baseline | §14.4 |
| 4 | Within 3 minutes **one** incident is open — also when two core replicas are running | §15.1, §15.3 |
| 5 | The database dependency is the highest-ranked root-cause candidate, explained by temporal precedence and trace evidence; checkout and gateway appear in the blast radius | §15.2 |
| 6 | From v0.6 the PostgreSQL connection-count metric is part of the incident's evidence | §13.2 |
| 7 | Slack and webhook notifications arrive at opening and at resolution | §15.5 |
| 8 | The fault ends; after the cool-down the incident resolves by itself. Triggered again, the same incident reopens | §15.1 |

The scenario is the shortest description of the product: independent signals, one incident, a ranked cause, every claim with its evidence.


## 22 Capacity planning

*Status: Planned. The formulas are stated here; their coefficients are measured by the Phase 7 benchmark and published. Until then no figure in this section is a promise.*

### 22.1 Inputs

| Symbol | Meaning | Source |
|:-------|:-----------------------------------------------|:------------------|
| R | Average events per second, per signal | the user's workload |
| P | Peak factor (peak rate ÷ average rate) | the user's workload |
| D | Retention in days | setting |
| `D_raw` | Retention of the raw topic in days | setting |
| RF | Kafka replication factor | deployment |
| s | Average size of an event inside a Kafka message, in bytes | **measured** |
| b | Bytes on disk per stored event after compression | **measured** |
| h | Free-space headroom for merges (a fraction) | **measured** |
| `T_core` | Events per second one core replica writes | **measured** |
| `T_an` | Events per second one analytics worker processes | **measured** |

### 22.2 Formulas

```
events per day          E  = R x 86,400
ClickHouse disk            = E x D x b x (1 + h)
Kafka disk (raw topic)     = E x D_raw x s x RF
core replicas              = ceil( R x P / T_core )   + 1 for availability
analytics workers          = ceil( R_logs x P / T_an )
partitions per topic      >= max( core replicas, analytics workers )
tail query load            = open tails / poll interval      (queries/s)
```

Notes:

- Partition count is the ceiling of parallelism for both consumers; it is chosen with room to grow, because raising it later re-distributes keys.
- With the key `hash(tenant, service)` one very loud service lands on one partition. The benchmark measures how far that hot partition falls behind (§16.3).
- Pre-aggregates, indexes and the `trace_summary` table add to b; the benchmark reports b per signal, including them.

### 22.3 What is known today

| Figure | Value | Nature |
|:------------------------------|:-----------------|:--------------------------|
| Log write rate of one core instance on a laptop | at least 5,000 logs/s | acceptance criterion of v0.1 — a target until measured |
| Analytics worker threshold | 3,000 logs/s | decision threshold of ADR-006, not a measurement |
| Bundle memory | fits a machine with 8 GB | design budget of v0.1 |

Worked example without coefficients: 1,000 logs per second is 86.4 million rows per day and 604.8 million rows at seven days of retention; multiplied by the measured b it gives the disk a user must provide.

### 22.4 Published by the benchmark (Phase 7)

Logs and spans per second per core replica; disk per one million events after compression; query p95 values; end-to-end ingest delay; detector delay; the hot-partition measurement. A synthetic generator (`scripts/loadgen`) exists from v0.1 and is the tool for all of them.


## Appendix A — Changes since the first edition

Each gap the master roadmap found in the first edition, and where this edition resolves it.

| Gap in the first edition | Resolution | Here | Phase |
|:------------------------|:--------------------------------------|:------|:----|
| Licence undecided | Apache-2.0 with DCO (ADR-001) | §19.1 | 1 |
| Next.js required a Node runtime | Vite + React static SPA (ADR-002) | §6 | 1 |
| No Redpanda option | Redpanda default in Compose, Kafka API throughout (ADR-003) | §6 | 1 |
| Persistence style and migration tool unclear | Spring Data JDBC, Flyway, ClickHouse migration runner (ADR-004) | §6, §9 | 1 |
| ClickHouse full-text search weak | Token index, materialised columns, exact token semantics | §9.2 | 1 |
| Figure 2 inconsistent | Analytics input is Kafka; ClickHouse only for baseline reads | §4.2 | 1, 3 |
| No normalised span event | `NormalizedSpanEvent` | §7.3 | 2 |
| Trace assembly and late spans undefined | Span-by-span write, completion window, "late" recompute (ADR-010) | §12.2 | 2 |
| Tail sampling against Collector scaling | Two-tier Collector topology | §8.6 | 2 |
| Drain3 state and replica consistency | Partition key, compacted-topic persistence, stable `pattern_id` | §14.1–2 | 3 |
| Where baselines live; warm-up | ClickHouse pre-aggregates; "learning" state | §14.3 | 3 |
| Python throughput risk | Measured threshold and Java fallback (ADR-006) | §14.5 | 3, 7 |
| Incident life cycle undefined | Opening, merging, closing, flapping, de-duplication (ADR-007) | §15.1 | 4 |
| Coordination between core replicas | Single orchestrator by advisory lock (ADR-008) | §15.3 | 4 |
| No alerting or notification | Five channels, routing, retry, history (ADR-012) | §15.5 | 4 |
| Source of deployment context | `service.version` change and CI webhook | §15.4 | 4 |
| Storage side of multi-tenancy | One table set, `tenant_id` first, central scope, quotas (ADR-009) | §9.4, §16.3 | 5 |
| Hot-partition risk | Key `hash(tenant, service)`; measured in the benchmark | §16.3, §22 | 5, 7 |
| Erasure of personal data | Lightweight-delete job with documented cost | §9.5 | 5 |
| Metrics pipeline undefined | Native ingest plus Prometheus adapter; temporality; cardinality (ADR-005, ADR-013) | §13 | 6 |
| Capacity formulas without coefficients | Filled by the Phase 7 benchmark | §22 | 7 |

Added by the Phase 1 roadmap, beyond the master roadmap's list:

| Addition | Here |
|:--------------------------------------------------------|:--------|
| Event identity from Kafka coordinates (ADR-015) | §8.4 |
| Principal-and-scope abstraction from the first endpoint (ADR-016) | §10.1 |
| Live tail as a repeated read through the query builder | §10.5 |
| Two kinds of ingestion failure, never mixed | §8.5 |
| Loopback-only default exposure while there is no authentication | §16.1 |
| Multi-architecture images and an 8 GB memory budget | §17.1 |
| The bundled Collector positioned as the SDK's OTLP endpoint | §8.1 |


## Appendix B — Decision index

Status as of 05.10.2026. No ADR file exists yet; ADR-001 to ADR-005 are written and accepted in WP1, ADR-015 and ADR-016 are opened there as drafts.

| ADR | Subject | Phase | Here |
|:--------|:------------------------------------------------------|:------|:------|
| 001 | Licence (Apache-2.0) and DCO — decided by the maintainers on 05.10.2026 | 1 | §19.1 |
| 002 | Frontend: Vite + React SPA | 1 | §6 |
| 003 | Compose backbone: Redpanda default, Kafka API | 1 | §6 |
| 004 | Persistence: Spring Data JDBC, Flyway, ClickHouse client and migration runner; Spring Boot 4.1.x line | 1 | §6, §9 |
| 005 | Metrics strategy — decision due at the start of Phase 6 | 1 / 6 | §13 |
| 006 | Analytics throughput measurement and the threshold for moving to Java | 3 / 7 | §14.5 |
| 007 | Incident life-cycle rules | 4 | §15.1 |
| 008 | Core replica coordination (advisory lock) | 4 | §15.3 |
| 009 | Tenant-isolation storage model and quotas | 5 | §9.4, §16.3 |
| 010 | Trace completion window and late-arrival semantics | 2 | §12.2 |
| 011 | Correlation evidence types and weights | 4 | §15.2 |
| 012 | Notification channels and delivery guarantees | 4 | §15.5 |
| 013 | Cardinality protection policy | 6 | §13.2 |
| 014 | Release, versioning and support policy | 7 | §18.3 |
| 015 | Event identity — closed in WP9 with measured behaviour | 1 | §8.4 |
| 016 | Principal and scope — closed in WP10 | 1 | §10.1 |


## Appendix C — Glossary

| Term | Meaning in DOP |
|:------------------|:------------------------------------------------------------|
| Anomaly | A detector's finding: an observed value outside its baseline for a scope and a window |
| Baseline | The expected value of a signal, read from per-minute pre-aggregates |
| Blast radius | The services affected by an incident, as a sub-graph of the service map |
| Control plane | Everything that is not telemetry: projects, users, incidents, rules — in PostgreSQL |
| Dead-letter topic | The Kafka topic for messages core could not process, each with its reason |
| Evidence | A typed, clickable fact that supports an anomaly or a root-cause candidate |
| Event backbone | The Kafka-API broker between the Collector, core and analytics |
| Incident | Several connected anomalies joined into one record with a life cycle |
| Pattern | A log template mined from many lines, with a stable identifier |
| Principal | Who is asking, for which tenant and project, with which permissions |
| RED | Rate, errors, duration — per service, computed from spans |
| Scope | The tenant and project boundary every query is built inside |
| Warm-up | The learning period in which a detector raises no alarm |
