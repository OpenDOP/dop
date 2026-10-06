# Roadmap

An English summary of the master roadmap (v1.0, 29.09.2026). The master roadmap is the
authoritative plan; this page exists so that the plan is readable in the repository's working
language. Where this page and an accepted ADR disagree, the ADR wins.

## Principles

1. **Vertical slice, not horizontal layer.** Logs finish end to end first, then traces, then
   analytics. No phase is backend-only or UI-only.
2. **Every phase is a release.** Phase close = Git tag + GitHub release notes + container
   images + a one-to-three-minute demo recording. Without exception.
3. **The ten-minute rule.** On a clean machine, a new user sees their own telemetry in the
   interface within ten minutes. Verified at every release from v0.1 onward. This is the single
   biggest determinant of adoption.
4. **Evidence before AI.** The deterministic pattern, anomaly and correlation engine is the
   core of the product. A language-model layer comes after v1.0.
5. **Decide, write an ADR, move on.** In a two-person team the most expensive thing is a
   decision left open. Every significant decision becomes an ADR in [adr/](adr/).
6. **No feature is merged without documentation.** Every user-visible feature ships with at
   least one documentation page in the same pull request.
7. **Hide operating cost from the small user.** Kafka, ClickHouse and PostgreSQL are part of
   the product, but the single-machine Compose profile must start them with one command.
8. **Protect the scope.** A request that does not belong to the current phase becomes an issue
   labelled `backlog`. Each phase defends its boundary with an explicit list of what is left
   out.

## The eight phases

| Phase | Name | Release | What the user gets | What it must prove |
|:--|:--|:--|:--|:--|
| 1 | Foundation and logs end to end | **v0.1** | Log Explorer and live tail; first log in ten minutes | A log sent by an SDK is ingested, stored, found by search and followed live; a message consumed twice produces one row; the quickstart runs in under ten minutes on 8 GB |
| 2 | Traces and service map | v0.2 | Trace waterfall, log-to-trace navigation, dependency graph | Spans assemble into a correct trace; a log line reaches its trace and back; the service map renders in under two seconds |
| 3 | Pattern mining and anomaly detection | v0.3 | Pattern view, Anomaly Center, four detectors | Log noise collapses into stable patterns; the detectors fire on known data and stay quiet on normal data; a detector states when it is still learning |
| 4 | Incidents, correlation, RCA, alerting | v0.4 | Incident Workspace, evidence-based root-cause ranking, Slack / webhook / PagerDuty | Independent anomalies join into one incident; every root-cause candidate's score is explainable line by line; notifications reach their channel |
| 5 | Multi-tenancy, auth, security hardening | v0.5 | Users, RBAC, API tokens, OIDC, tenant isolation, audit | One tenant cannot read another's data by any path; the principal and scope abstraction built in Phase 1 carries real identities without a rewrite |
| 6 | Metrics | v0.6 | OTel metrics ingest, RED and infrastructure panels, metric anomaly evidence | Delta and cumulative senders produce the same graph; percentiles computed from histograms are correct against known data; exceeding the cardinality limit drops series and warns instead of failing ingest |
| 7 | Production readiness and the v1.0 launch | **v1.0** | Helm chart, backup and upgrade guides, published benchmarks, signed images, versioned docs site | DOP runs on a cluster under an SRE team's operational practices, with published capacity numbers and signed artefacts |
| 8 | After v1.0 | v1.x | AI explanation, advanced detectors, ecosystem, governance | Continuous; no fixed end |

**Why this order.** Logs have the lowest barrier to entry, so the first users arrive with them.
Traces are the foundation of correlation and must precede analytics. Pattern and anomaly
detection are the raw material of the incident layer. Authentication and multi-tenancy come
after the product's "wow" moment in Phase 4, which is what attracts teams and companies.
Metrics are deliberately last: a metric dashboard is not what makes DOP different and the
Prometheus and Grafana ecosystem is already strong there — but a v1.0 without metrics would
read as an incomplete observability platform, so they land before 1.0.

The total projected effort to v1.0 is roughly 62–66 weeks, with about 15% buffer already inside
each phase. That assumes two maintainers at a combined 60–80 hours per week. At part-time
capacity the calendar multiplies by 1.5–2; **the order and the acceptance criteria do not
change.**

## Dates

Dates exist for **Phase 1 only**. Later phases are sized in weeks, not scheduled.

| | |
|:--|:--|
| Phase 1 | 10 weeks — Monday **05.10.2026** to Friday **11.12.2026** |
| | three weeks of foundation, seven weeks of logs; the buffer is inside these figures |
| Release | **v0.1.0 "Log Explorer"**, targeted **11.12.2026** |

Phase 1 merges what the master roadmap called Faz 0 and Faz 1, by maintainer decision D1.1,
because Faz 0 had not been executed and would have ended in no release.

## The release train

- **SemVer.** Until v1.0 the project is on `0.x`: **a minor version is a phase close**, with
  patch releases as needed. After v1.0: monthly minors, patches as needed, breaking changes
  only in a major.
- **Automation.** Conventional Commits feed release-please, which produces the changelog, the
  tag and the GitHub release. CI pushes images to GHCR on a tag — `dop-core`, `dop-analytics`,
  `dop-web` — signed from Phase 7.
- **Compatibility.** Protobuf contracts are backward-compatible from Phase 3; a field is
  removed only after a deprecation period and only in a major. The REST API `/api/v1` is frozen
  at v1.0. ClickHouse and PostgreSQL migrations are forward-only and automatic; rollback is a
  runbook.
- **Supported versions.** After v1.0, security patches for the last two minor versions, stated
  in [SECURITY.md](../SECURITY.md).
- **Release notes.** Format: *Highlights / Breaking / Upgrade notes / Contributors*. Every
  release note carries a screenshot or a GIF.

## Correction against the master roadmap

Both the master roadmap and the Phase 1 roadmap specify **Spring Boot 3.x**. Open-source
support for the entire 3.x line ended on **30.06.2026**. core is therefore built on the
**Spring Boot 4.1.x** line with Java 21 unchanged. See
[ADR-004](adr/004-persistence-and-framework-line.md) for the evidence and the options that were
rejected. The roadmap documents themselves have not been reissued.
