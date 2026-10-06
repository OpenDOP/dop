# D2 — Upstream Reality Report

Deliverable **D2** of Phase 1: the pinned version and the measured behaviour of each upstream
component, and a keep-or-adjust verdict for every roadmap assumption that the measurements
touch.

**Source of every figure on this page: the WP1 Round 0 discovery, run 05.10.2026–06.10.2026.**
Nothing here is taken from memory or from documentation alone. Entries are added by the Round 0
of each later work package; this is the first set.

Measurement host: Apple M1 Pro, `linux/arm64`, Docker Desktop 27.3.1 with a 7.654 GiB VM.
**No measurement has yet been repeated on an amd64 machine.**

## Pinned versions (D1.8)

Image digests are multi-arch index digests; pinning by these keeps both architectures working.
Every image below carries **both `linux/amd64` and `linux/arm64`** manifests.

| Component | Pin | Image digest |
|:--|:--|:--|
| Java 21 JDK (Eclipse Temurin) | `eclipse-temurin:21.0.12.1_1-jdk-noble` | `sha256:b468c3fc688b14450571494f588bd939378e7fd542ed5a73f8efc13f17872a87` |
| Spring Boot | **4.1.x line** — exact patch pinned in WP3 (ADR-004) | — |
| Gradle | `9.8.0`; wrapper SHA-256 `238e777fcddd7e34f9708186085def2abd6e08e658505b38718d79d74c21abd5` | — |
| Python | `python:3.12.15-slim-trixie` | `sha256:02108f5d322dd89f1c9e552442c25acb0543dfdbc455693a5599624f20d9155d` |
| uv | `0.12.23` | — |
| ruff | `0.16.10` | — |
| Node.js | `node:24.21.0-trixie-slim` | `sha256:8ec5d7557396cfe32d21c3f9c13072355ceab22b584578ca4bb28af31120cffe` |
| Buf CLI | `1.73.0` | — |
| ClickHouse | `clickhouse/clickhouse-server:26.8.18.2` (LTS) | `sha256:d97c9867f770fab06113bfe82f2cd17dcc9535f0773972469a2464b4fefc6a48` |
| Redpanda | `redpandadata/redpanda:v26.2.3` | `sha256:9e83cfa99278f30d0133271c26bf670cd69c94ffa6ba0b42830dd0c3bd9dcfd9` |
| Apache Kafka (KRaft) | `apache/kafka:4.3.1` — **unverified**, see below | `sha256:77e3df9054047a88b520d0cc46e16696d3b22022e1d580aeccd2632df6532837` |
| PostgreSQL | `postgres:18.6-trixie` | `sha256:5a5a84b19854a9ffaa54082c166ff4ec27473a361e496e5ea167f298f2da9722` |
| OTel Collector contrib | `otel/opentelemetry-collector-contrib:0.162.0` | `sha256:39923a8e431bd1f57be82411999d389fcfe40857492e4365456d97a4c1f74be6` |
| Nginx | `nginx:1.30.5` (stable line) | `sha256:b972f831f200b19ef0767938224f9711e74cd783718738cd7405d5cabf75c442` |

Pins are refreshed by hand at each phase close until Renovate arrives in Phase 5.

## Measured behaviour

Each component was pulled for `linux/arm64`, started once with defaults, and measured. The
Collector ran a minimal `otlp` receiver → `debug` exporter configuration. Pipeline behaviour was
**not** tested.

| | Redpanda | ClickHouse | PostgreSQL | OTel Collector contrib |
|:--|:--|:--|:--|:--|
| Version the process reports | `v26.2.2` | `26.8.18.2` | `PostgreSQL 18.6 (Debian 18.6-1.pgdg13+2)` | `otelcol-contrib version 0.162.0` |
| Compressed image size (arm64) | 118.7 MB | 262.6 MB | 161.1 MB | 102.6 MB |
| Pull time | 8 s | 16 s | 12 s | 8 s |
| Start to ready | 0.82 s | 1.15 s | 0.81 s incl. `initdb` | 0.18 s |
| Idle memory after 60 s | **584.8 MiB** | **449 MiB** | **28.6 MiB** | **43.6 MiB** |
| Default listening ports | 9092 Kafka API, 8081 Schema Registry, 8082 HTTP proxy, 9644 admin, 33145 internal RPC | 8123 HTTP, 9000 native, 9004 MySQL protocol, 9005 PostgreSQL protocol, 9009 interserver | 5432, `listen_addresses = *` | 4317 OTLP/gRPC, 4318 OTLP/HTTP, 55679 exposed |

**Total idle memory of the four: 1,106 MiB ≈ 1.08 GiB** — about 14% of the 7.654 GiB the Docker
VM had. Idle infrastructure is not the risk to the 8 GB target; the configured ceilings are.

### Redpanda

- Started as `redpanda start --mode dev-container --smp 1`.
- Maximum Kafka message/batch size: setting **`kafka_batch_max_bytes`**, default **1048576
  bytes (1 MiB)**. Related: `kafka_request_max_bytes = 104857600`, `fetch_max_bytes = 57671680`,
  `log_segment_size = 134217728`.
- The 584.8 MiB figure is the `dev-container` mode figure. Production mode reserves memory up
  front via Seastar and is substantially larger.
- Measured at `v26.2.2`; the pin is `v26.2.3`. The GitHub releases API still reports `v26.2.2`
  as latest and is **not** a reliable version source for Redpanda — Docker Hub and the Redpanda
  docs are.

### ClickHouse

- `max_server_memory_usage` resolved to **6,491,096,186 bytes (6.05 GiB)** from a config default
  of `0`, via `max_server_memory_usage_to_ram_ratio = 0.9`. The ceiling is derived from detected
  RAM, so it will differ on a real 8 GB host.
- `max_memory_usage` (per query) = `0` (unlimited); `max_memory_usage_for_user` = `0`;
  `max_bytes_before_external_group_by` = `0` (never spills to disk). **One heavy `GROUP BY` can
  take the whole machine.**
- Default user **`default`**, auth type `plaintext_password`, with an **empty password** —
  confirmed by logging in with `--password ''`. A fresh container is open to anyone who can
  reach 8123 or 9000.
- The image initialises an outbound crash reporter on startup.

### PostgreSQL

- Defaults: `shared_buffers = 16384 × 8kB`, `work_mem = 4096 kB`,
  `maintenance_work_mem = 65536 kB`, `effective_cache_size = 524288 × 8kB`,
  `max_connections = 100`.
- **The Postgres 18 image changed its data mount point.** A volume at
  `/var/lib/postgresql/data` — the convention through Postgres 17 — makes Postgres 18 refuse to
  start. The mount must be `/var/lib/postgresql`. This was hit in the discovery.

### OpenTelemetry Collector contrib

Presence confirmed from the binary's own `components` listing:

| Component | Present | Stability (traces / metrics / logs) |
|:--|:--|:--|
| `otlp` receiver | yes | Stable / Stable / Stable |
| `memory_limiter` processor | yes | Beta / Beta / Beta |
| `batch` processor | yes | Beta / Beta / Beta |
| `redaction` processor | yes | **Beta / Alpha / Alpha** |
| `transform` processor | yes | Beta / Beta / Beta |
| `kafka` exporter | yes | Beta / Beta / Beta |
| `loadbalancing` exporter | **not under that name** — registered as **`load_balancing`** | Beta / Alpha / Beta |

Two renames that will break any configuration written from older examples:

- `loadbalancing` → **`load_balancing`**.
- The `otlp` **exporter** no longer exists; it is split into **`otlp_grpc`** and **`otlp_http`**
  (both Stable).

A first-party `clickhouse` exporter also exists (Beta traces, Alpha metrics, Beta logs); ADR-003
records why DOP does not use it.

## Verdicts on the roadmap's assumptions

| Assumption in the roadmaps | Verdict | Acted on by |
|:--|:--|:--|
| Core on **Spring Boot 3.x** | **Adjust.** OSS support for the whole 3.x line ended 30.06.2026. core is built on **4.1.x** with Java 21 unchanged | ADR-004; patch pinned in **WP3** |
| Gradle as the core build tool | **Adjust.** Gradle 9.8.0 requires Spring Boot 4.x — Spring Boot 3.5 supports only 7.6.4+/8.4+. The framework decision and the Gradle pin are one decision | ADR-004; **WP3** |
| **Java 21** for core | **Keep.** Supported by both Spring Boot lines; Temurin 21 is LTS to at least Dec 2029 | **WP3** |
| **Python 3.12** for analytics | **Keep.** EOL 31.10.2028 | **WP4** |
| **Node LTS** for the web build | **Keep**, with a date. Node 24 "Krypton" enters maintenance on **20.10.2026** and Node 26 becomes LTS on **28.10.2026** — both within two weeks. The pin stays at 24.21.0 for v0.1 and is revisited at the phase close | **WP4** |
| **Redpanda** as the default backbone | **Keep.** 118.7 MB image, 0.82 s to ready, 585 MiB idle in dev-container mode | ADR-003; re-measured in **WP5** |
| **Kafka KRaft** as an alternative profile | **Keep, unverified.** `apache/kafka:4.3.1` is recorded but was never started. It counts as unverified until its Compose profile exists | **WP5** |
| **ClickHouse** for telemetry | **Keep**, but its defaults must be overridden. Unlimited per-query memory, no spill, a 6.05 GiB server ceiling and a password-less `default` user are all wrong for a shipped profile | **WP5** (limits), **WP8** (schema) |
| **PostgreSQL** for the control plane | **Keep.** 28.6 MiB idle is the cheapest component measured. The 18.x data-directory change must be honoured in the Compose file | **WP5** |
| **OTel Collector contrib** as the bundled entry point | **Keep.** All required components are present | **WP7** |
| Collector `redaction` processor usable for logs | **Adjust the expectation.** It is **Alpha** for logs, not Beta. If redaction of log bodies is a product promise, it rests on an Alpha component | **WP7** |
| The pipeline `otlp → memory_limiter → batch → redaction → kafka` | **Keep as a shape**, but component names must be taken from the 0.162.0 listing, not from older examples | **WP7** |
| Nginx serving the SPA | **Keep.** `nginx:1.30.5` is the current stable line | **WP4** |
| **Protobuf + Buf** for contracts | **Keep.** Buf CLI 1.73.0 pinned; nothing measured beyond the version | **WP2** |
| **8 GB laptop** as the target | **Keep, unproven.** Idle infrastructure is 1.08 GiB, but the measurement host had 16 GiB of RAM and ClickHouse sizes its ceiling from detected RAM. The budget is not proven until it is measured on real 8 GB hardware | **WP5**, **WP16** |
| The **ten-minute rule** | **Not measurable yet.** Pull times total 44 s for four images, which is the only part measured | **WP16** |
| GHCR, Discussions, Projects and Actions free to a public repository | **Keep.** The repository is public and the org exists; nothing contradicted this | **WP1**, **WP6** |

## What is NOT measured yet

Named explicitly so that no later package assumes it was done.

| Not measured | Why it matters | Measured by |
|:--|:--|:--|
| **Collector `kafka` exporter behaviour** — message shape, `otlp_proto` encoding, how one export request maps to one Kafka message, delivery and retry settings, how it behaves when the broker is unavailable | The whole ingestion contract depends on the shape of what lands on `dop.otlp.logs.v1` | **WP7** |
| **Collector `redaction` processor behaviour** — which fields it reaches, what it does to log bodies as opposed to attributes, what Alpha status means in practice for logs | It is the first privacy layer and it is Alpha for logs | **WP7** |
| **The ClickHouse Java client** (`clickhouse-java`) — version, batch insert behaviour, connection and timeout defaults, how it reports a partial failure | ADR-004 commits to it; the idempotent write and the offset-commit boundary depend on how it acknowledges | **WP8** |
| **Apache Kafka 4.3.1 in KRaft mode** — startup, memory, the equivalent of `kafka_batch_max_bytes`, behaviour against the same Collector configuration | The profile is promised in ADR-003 and has never been run | **WP5** |
| **Redpanda `v26.2.3`** — the pinned patch; `v26.2.2` was measured | The pin and the measurement disagree by one patch | **WP5** |
| **ClickHouse read technique for duplicate elimination** — `FINAL` versus an aggregating read, and its cost | D1.11 says the technique is chosen by measurement | **WP8** |
| **Anything on `linux/amd64`** — every figure here is arm64 | Half the user base and half the release matrix | **WP5**, **WP16** |
| **Anything on a real 8 GB host** — the measurement machine had 16 GiB | The memory budget is the product's defining constraint | **WP5**, **WP16** |
| **Buf**, **uv**, **ruff**, **Gradle**, **Node** beyond their version numbers | Only versions were established; no behaviour was exercised | **WP2**, **WP3**, **WP4** |
