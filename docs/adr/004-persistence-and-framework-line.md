# ADR-004: Persistence layer and the Spring Boot line

- **Status:** Accepted
- **Date:** 06.10.2026
- **Deciders:** @merttemiz, @hasan4adnan

## Context

Two questions are settled here because they are answered by the same build.

**Persistence.** DOP has two stores with very different jobs. PostgreSQL is a control plane of
a handful of tables — projects, data sources — read and written in simple aggregates.
ClickHouse holds all telemetry, is written in batches and read through one central query
builder. Schemas in both change only by migration.

**Framework line.** Both the master roadmap and the Phase 1 roadmap name "Spring Boot 3.x". The
WP1 Round 0 discovery, run on 05.10.2026, found that this is no longer a supported choice. The
authoritative source `https://api.spring.io/projects/spring-boot/generations` reports:

| Generation | OSS support ends | Commercial support ends |
|:-----------|:-----------------|:------------------------|
| 3.3.x | 30.06.2025 | 30.06.2026 |
| 3.4.x | 31.12.2025 | 31.12.2026 |
| 3.5.x | **30.06.2026** | 30.06.2032 |
| 4.0.x | 31.12.2026 | 31.12.2027 |
| 4.1.x | 31.07.2027 | 31.07.2028 |

Open-source support for the **whole** 3.x line ended on 30.06.2026, three months before the
project started. The latest generally available release is 4.1.1. Java support is not the
constraint: Spring Boot 3.5 requires Java 17 and is tested to 25, Spring Boot 4.1 requires
Java 17 and is tested to 26, so Java 21 LTS satisfies both
(`https://docs.spring.io/spring-boot/system-requirements.html`). Gradle is a constraint:
Spring Boot 3.5 supports Gradle 7.6.4+ or 8.4+ and **not** Gradle 9, while Spring Boot 4.1
supports Gradle 8.14+ or 9.x and the current Gradle release is 9.8.0.

The repository was empty when this was discovered, so there was nothing to migrate.

## Decision

**Persistence.** PostgreSQL is accessed with **Spring Data JDBC**, with versioned migrations
run by **Flyway**. ClickHouse is accessed with the **official `clickhouse-java` client**, with
its own small SQL migration runner so that ClickHouse schemas are versioned too. Java 21
**virtual threads** are enabled on the I/O-bound consumer and query paths.

**Framework line.** core is built on the **Spring Boot 4.1.x** line with **Java 21**. The exact
patch version is pinned in WP3.

## Alternatives considered

| Alternative | Why not |
|:------------|:--------|
| JPA / Hibernate for the control plane | Entity life-cycle and lazy-loading complexity with no use in a control plane of two tables. |
| An ORM or a third-party migration tool for ClickHouse | ClickHouse is not a relational target for an ORM; the official client plus a small runner is less code and fewer surprises than bending a tool built for OLTP. |
| Flyway for ClickHouse as well | Not a supported ClickHouse path at the level DOP needs; the runner is a few dozen lines. |
| **Stay on Spring Boot 3.5.x** | No free security patches since 30.06.2026. Shipping an open-source observability product, which terminates untrusted telemetry, on an unpatched framework is not defensible. It would also pin Gradle to 8.x. |
| **Spring Boot 3.5.x with commercial support** | Keeps the line patched to 2032, but adds a paid dependency to an Apache-2.0 community project. |
| **Spring Boot 4.0.x** | Also on Framework 7, but its OSS support ends 31.12.2026 — the migration would be done and then immediately repeated for 4.1. |
| Dropping Spring Boot — plain Spring Framework 7, Quarkus or Helidon | A far larger change than the problem requires, and it discards the Spring for Apache Kafka and Spring Data integration the design already assumes. |

## Consequences

- Both roadmaps are now **wrong on this point**, and this ADR is the record of the change. The
  architecture reference has been corrected; the roadmap documents have not been reissued.
- The baseline is Spring Framework 7, Jakarta and Servlet 6.1 with Tomcat 11, not the
  Framework 6.2 / Tomcat 10.1 baseline a 3.x project would have had. Any example or recipe
  written for Spring Boot 3 needs checking before it is copied.
- Gradle 9.8.0 is usable, which it would not have been on the 3.x line.
- OSS support for 4.1.x ends 31.07.2027, which is inside the projected v1.0 timeline. **The
  Spring Boot line is reviewed at every phase close, together with the version-pin refresh.**
  The project moves to the next supported minor before open-source support for the line it is
  on ends, so that it is never again running on an unsupported framework.
- Spring Data JDBC gives no lazy loading and no dirty checking; aggregates must be loaded and
  saved explicitly. For two tables that is the point, not a limitation.
- ClickHouse migrations are DOP's own code and therefore DOP's own risk. The runner needs tests
  from the day it exists (WP8).
