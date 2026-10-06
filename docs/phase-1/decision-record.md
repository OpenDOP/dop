# D1 — Phase 1 Decision Record

Deliverable **D1** of Phase 1. Every decision item of the Phase 1 roadmap §5.1, with its
status, its owner and the date or work package that settles it.

**Status values.** *Closed* — decided and dated. *Adopted* — the roadmap wrote it as the
default and no maintainer objected within WP1, so it stands. *Fixed* — a consequence of a
master-roadmap principle, not open in this phase. *Owned* — not yet settled; the named work
package closes it.

Maintainer A ("platform") = @merttemiz. Maintainer B ("intelligence and experience") =
@hasan4adnan.

| ID | Item | Decision | Status | Owner | Date / closes with |
|:--|:--|:--|:--|:--|:--|
| D1.1 | Phase merge and calendar | Faz 0 is delivered inside Phase 1; ten weeks; v0.1.0 targeted for 11.12.2026 | Closed | Both | 05.10.2026 |
| D1.2 | Licence and contribution sign-off (ADR-001) | Apache-2.0 with DCO (`Signed-off-by`), no CLA | Closed | Both | 05.10.2026 |
| D1.3 | Frontend (ADR-002) | Vite + React SPA served by Nginx; no Node runtime in the shipped image | Adopted | Both | 06.10.2026 |
| D1.4 | Event backbone in Compose (ADR-003) | Redpanda by default; the product is written against the Kafka API; Kafka KRaft as a separate profile | Adopted | Both | 06.10.2026 — test scope of the Kafka profile narrowed in WP5 |
| D1.5 | Persistence (ADR-004) | Spring Data JDBC and Flyway for PostgreSQL; the official `clickhouse-java` client and a small own SQL migration runner for ClickHouse | Adopted | Both | 06.10.2026 |
| D1.6 | Metrics strategy (ADR-005) | Not decided now. ADR written with status "decision due at the start of Phase 6" | Fixed | Both | ADR-005 superseded at the start of Phase 6 |
| D1.7 | Repository identity | GitHub `OpenDOP/dop`; images `ghcr.io/opendop/dop-core`, `dop-analytics`, `dop-web`; Java base package `io.github.opendop`; Protobuf root `dop.*`; any published package named `opendop` / `@opendop/*` because the bare name `dop` is taken on PyPI and npm | Closed | Both | 05.10.2026 |
| D1.8 | Version pins | The consolidated table of the WP1 Round 0 report, recorded in [upstream-reality.md](upstream-reality.md). Rulings: Spring Boot **4.1.x** with Java 21 (both roadmaps say 3.x; OSS support for 3.x ended 30.06.2026 — ADR-004); Gradle 9.8.0; **Node 24.21.0 holds through v0.1 and is reviewed at the Phase 1 close**; Redpanda pinned `v26.2.3`; Apache Kafka 4.3.1 recorded but unverified until its Compose profile exists. **The Spring Boot line is reviewed at every phase close together with the pin refresh, and moves to the next supported minor before open-source support for the current one ends** | Closed | Maintainer A | 06.10.2026 — Spring Boot patch pinned in WP3; Redpanda re-measured and Kafka verified in WP5 |
| D1.9 | Repository language | English for code, documentation, issues and ADRs; the master roadmap's summary published in English as [docs/roadmap.md](../roadmap.md) | Adopted | Both | 06.10.2026 |
| D1.10 | Event identity (ADR-015) | Deterministic identifier derived from topic, partition, offset and the record's ordinal inside the message. Redelivery and replay produce the same identifier. Content hashing is rejected | Owned — ADR-015 is a **Draft** | Maintainer A | Closed by **WP9** |
| D1.11 | Duplicate elimination in ClickHouse | `ReplacingMergeTree` with the identifier as the last element of the ordering key; the read path removes not-yet-merged duplicates. The read technique is chosen by measurement | Owned | Maintainer A | Closed by **WP8** |
| D1.12 | Free-text semantics | Token search, case-insensitive, all tokens must match; no substring and no regular-expression search in v0.1; stated in the UI hint and in the known limitations | Adopted | Both | 06.10.2026 |
| D1.13 | Live-tail mechanism | The tail polls ClickHouse with an ingestion-time cursor through the same query builder as search; no second delivery path | Adopted | Both | 06.10.2026 |
| D1.14 | Principal and scope (ADR-016) | Every handler receives a principal; every query is built from a scope. In v0.1 one built-in principal with the default tenant and project | Fixed — ADR-016 is a **Draft** | Both | Closed by **WP10** |
| D1.15 | Default retention | 7 days in the Compose profile; one setting; applied by the retention job | Adopted | Both | 06.10.2026 |
| D1.16 | Default network exposure | UI/API and OTLP ports published on `127.0.0.1` only; data stores not published at all in the quickstart profile; opening them is an explicit setting with a documented warning | Fixed | Both | 06.10.2026 |
| D1.17 | Analytics worker in v0.1 | Built, tested and published; not started by the quickstart profile, because it has no function before Phase 3 | Adopted | Both | 06.10.2026 |
| D1.18 | Attribute values | Stored as strings in `Map` columns; non-string OTLP values rendered canonically (numbers, booleans) or as JSON (arrays, maps); typed attribute search is not offered in v0.1 | Adopted | Both | 06.10.2026 |
| D1.19 | Severity | OTLP severity number kept as is; six display levels (TRACE, DEBUG, INFO, WARN, ERROR, FATAL) derived from its ranges; unspecified shown as UNSET | Adopted | Both | 06.10.2026 |
| D1.20 | Materialised attribute columns | A short initial list from the OpenTelemetry semantic conventions — environment, host, Kubernetes namespace and pod — extended only by migration | Owned | Maintainer A | Fixed in **WP8** |
| D1.21 | Topics | `dop.otlp.logs.v1` and `dop.otlp.logs.dlq.v1`; partition count and raw-topic retention fixed for a laptop profile | Owned | Maintainer A | Fixed in **WP7** Round 0 |
| D1.22 | Interface language | English only; no translation framework in v0.1 | Adopted | Both | 06.10.2026 |
| D1.23 | Blog venue and announcement | Where the first post is published, who writes it, and the exact wording of the single community post | Owned | Maintainer B | **Week 9** |
| D1.24 | Messages that cannot be decoded | Sent to the dead-letter topic with a reason; never guessed, never silently dropped | Fixed | Both | 06.10.2026 |

## ADRs produced by this deliverable

| ADR | Title | Status |
|:--|:--|:--|
| [001](../adr/001-licence-and-dco.md) | Licence and contribution sign-off | Accepted |
| [002](../adr/002-frontend-vite-react-spa.md) | Frontend — Vite + React single-page application | Accepted |
| [003](../adr/003-event-backbone-redpanda-kafka-api.md) | Event backbone — Redpanda by default, Kafka API | Accepted |
| [004](../adr/004-persistence-and-framework-line.md) | Persistence layer and the Spring Boot line | Accepted |
| [005](../adr/005-metrics-strategy.md) | Metrics strategy | Accepted as a record; decision due at the start of Phase 6 |
| [015](../adr/015-event-identity.md) | Event identity and the idempotent write | **Draft** — closed by WP9 |
| [016](../adr/016-principal-and-scope.md) | Principal and scope | **Draft** — closed by WP10 |

## Items of WP1 that this document does not close

These are WP1 tasks that are GitHub settings or process, not files, and are performed by the
maintainers:

- Enabling Discussions and private vulnerability reporting.
- Creating the public project board.
- Applying the label scheme.
- Branch protection on `main`: **pull request required and no force push, applied now**;
  the required-status-checks rule is **deferred to WP6**, when CI exists to provide the
  checks (maintainer decision, 06.10.2026).

As of 06.10.2026 none of these is in place; the WP1 Round 0 report records the measured state.

## Further decisions taken in WP1 (06.10.2026)

Not D1 items in the Phase 1 roadmap, but settled in WP1 and recorded here so they are not
re-opened.

| Item | Decision | Status | Owner | Date / closes with |
|:--|:--|:--|:--|:--|
| Label scheme | GitHub's default **`good first issue`** and **`help wanted`** (with spaces) are used instead of the hyphenated `good-first-issue` / `help-wanted` of Phase 1 roadmap §6.1. No hyphenated copies are created. The rest of the scheme — `area/*`, `phase/1`–`phase/8`, `backlog` — stands | Closed | Both | 06.10.2026 |
| Branch protection staging | `main` is protected now with pull request required and no force push; required status checks are added in **WP6**, when CI exists | Closed | Both | 06.10.2026 — completed by **WP6** |
| User-facing UI port | The address and port on which the interface is published are **not** named in any document yet; they are fixed by **WP5** with the Compose stack. `README.md` stays silent on the port until then | Owned | Maintainer A | Fixed in **WP5** |
