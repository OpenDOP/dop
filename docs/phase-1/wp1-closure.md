# WP1 — Closure Record

**Work package:** WP1 — Discovery, Decision Record and Repository Surface
**Phase:** 1 (v0.1.0 "Log Explorer")
**Opened:** 05.10.2026 — **Closed:** 06.10.2026
**Owners:** both maintainers (@merttemiz, @hasan4adnan)
**Rounds:** Round 0 (read-only discovery), Round 1 (repository surface), Round 1b (correction)

## 1. Result

WP1 is closed. The repository is public under Apache-2.0, carries its contribution,
security and conduct policies, seven Architecture Decision Records, the architecture
reference, the roadmap summary, the Phase 1 decision record (D1) and the first entries
of the upstream reality report (D2). No application code exists yet; that starts with WP2.

The settings listed in section 6 are the only items still to be confirmed. They do not
block WP2.

## 2. What was delivered

| Deliverable | Where |
|:--|:--|
| Licence and notice | `LICENSE`, `NOTICE` |
| Front door | `README.md` |
| Contribution, security and conduct policies | `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md` |
| Standing rules for AI coding assistants | `CLAUDE.md` |
| Repository conventions | `.editorconfig`, `.gitignore` |
| Directory layout with ownership | `contracts/`, `core/`, `analytics/`, `web/`, `infrastructure/`, `examples/`, `scripts/` — one `README.md` each |
| Architecture reference, second edition | `docs/architecture/architecture-reference.md` |
| ADR template and seven ADRs | `docs/adr/` |
| Roadmap summary | `docs/roadmap.md` |
| D1 — Phase 1 decision record | `docs/phase-1/decision-record.md` |
| D2 — upstream reality report, first entries | `docs/phase-1/upstream-reality.md` |
| GitHub metadata | `.github/CODEOWNERS`, pull-request template, issue templates |

33 files. First commit `b2be9b2` (licence alone, directly on `main`); the remaining eight
commits arrived through pull request #1 from `docs/repository-surface`, merged with
"rebase and merge" on 06.10.2026. `main` holds nine commits, head `4366354`. Every
commit is signed off.

## 3. Acceptance criteria

| Criterion (Phase 1 roadmap §6.1) | Status | Evidence |
|:--|:--|:--|
| D1 closed: each item decided or owned with a date | Met | `docs/phase-1/decision-record.md` |
| Seven ADR files present, five accepted | Met | ADR-001 to ADR-005 accepted; ADR-015 and ADR-016 drafts |
| A visitor reaches licence, contribution rules, security policy and roadmap from the README in one click each | Met | Four links in the README's "Licence and contributing" section; checked on the public repository page |
| The architecture reference no longer contradicts ADR-002 or itself | Met | Second edition: Vite + React frontend table, corrected Figure 2 |
| Phase 1 roadmap re-issued as Revision 2 if a decision changes scope | Not needed | No decision of WP1 changes the scope of Phase 1 (section 5) |

## 4. Decisions closed in WP1

| Item | Decision | Date |
|:--|:--|:--|
| D1.1 | Faz 0 delivered inside Phase 1; ten weeks; v0.1.0 targeted 11.12.2026 | 05.10.2026 |
| D1.2 / ADR-001 | Apache-2.0, DCO sign-off, no CLA | 05.10.2026 |
| D1.7 | GitHub `OpenDOP/dop`; images `ghcr.io/opendop/dop-core`, `dop-analytics`, `dop-web`; Java base package `io.github.opendop`; Protobuf root `dop.*`; published packages named `opendop` / `@opendop/*` | 05.10.2026 |
| D1.8 | Version pins of the Round 0 report, recorded in D2 | 06.10.2026 |
| ADR-004 | Spring Boot 4.1.x with Java 21 | 06.10.2026 |
| Proposed items of D1 | Adopted; no maintainer objected | 06.10.2026 |
| Roles | Maintainer A (platform) = @merttemiz; Maintainer B (intelligence and experience) = @hasan4adnan | 06.10.2026 |

ADR-001 to ADR-005 are accepted by both maintainers. Pull request #1 was merged by its
author before the second maintainer had reviewed it; Maintainer A reviewed and accepted
the ADRs afterwards, on 06.10.2026. The branch rule of section 6 prevents a repetition.

## 5. Deviations from the roadmaps

| Roadmap says | What was done | Why |
|:--|:--|:--|
| "Spring Boot 3.x" (both roadmaps, D1.8) | Spring Boot 4.1.x | Open-source support for the whole 3.x line ended on 30.06.2026 (Round 0). The repository was empty, so nothing had to be migrated. Recorded in ADR-004. Not a scope change |
| Labels `good-first-issue`, `help-wanted` | GitHub's defaults `good first issue`, `help wanted` | GitHub's contribution pages recognise the default names |
| Branch protection with required status checks | Pull request required and no force push now; required checks added in WP6 | No CI exists before WP6 |
| "Move the architecture reference to Markdown; apply ADR-002; correct Figure 2" | The reference was re-issued as a second edition before it entered the repository | The first edition described an earlier form of the project; the second edition folds in all 21 gaps of the master roadmap's review |
| Directory layout "exactly as written" | `docs/phase-1/`, `NOTICE` and `.gitignore` added | D1 and D2 needed a home; the architecture reference §5 was amended in the same package |
| Every package reaches `main` through pull requests | The first commit (licence alone) was pushed directly | A pull request needs an existing branch |

## 6. Repository settings

Confirmed:

- [x] Repository public; Apache-2.0 detected by GitHub
- [x] GitHub Discussions enabled (organisation and repository)
- [x] Repository description set
- [x] Merged branch `docs/repository-surface` deleted
- [x] Two organisation members

To be confirmed by the maintainers — tick before committing this file; anything left
unticked is carried into WP2 as an open item:

- [ ] Private vulnerability reporting enabled (the link in `SECURITY.md` depends on it)
- [ ] "Require contributors to sign off on web-based commits" enabled
- [ ] "Automatically delete head branches" enabled
- [ ] Both organisation members hold the Owner role
- [ ] Labels created: nine `area/*`, eight `phase/*`, `backlog`
- [ ] Public project board created and linked to the repository
- [ ] Ruleset on `main`: pull request required with one approval, force pushes blocked, deletion restricted

## 7. Carried forward

Facts measured in Round 0 that later packages must act on. Details are in
`docs/phase-1/upstream-reality.md`.

| Finding | Acts in |
|:--|:--|
| ClickHouse image: default user without password, memory ceiling at 0.9 × RAM with no per-query limit, crash reports sent to an external endpoint by default | WP5 |
| PostgreSQL 18 image: data volume is mounted at `/var/lib/postgresql` | WP5 |
| Redpanda pin v26.2.3 was measured as v26.2.2; re-measure | WP5 |
| Test scope of the Kafka KRaft profile to be narrowed | WP5 |
| Interface port to be fixed and written into the README | WP5 |
| Redpanda default `kafka_batch_max_bytes` is 1 MiB; batch size, exporter limit and broker limit are chosen together | WP7 |
| Collector contrib 0.162.0: redaction processor is Alpha for logs; `loadbalancing` is now `load_balancing`; the `otlp` exporter is split into `otlp_grpc` and `otlp_http` | WP7; Phase 2 |
| Spring Boot 4.1.x compatibility of Spring for Apache Kafka, Spring Data JDBC, Flyway and Testcontainers; exact patch pin | WP3 Round 0 |
| Required status checks and the DCO check on `main`; Docker Hub anonymous pull limit | WP6 |
| Stock macOS ships GNU Make 3.81 | Any package that adds a Makefile |
| Memory figures were taken on arm64 with a 7.65 GiB Docker VM; re-measure on a real 8 GB amd64 machine | WP16 |
| Spring Boot line and all pins reviewed | Phase 1 close |

Not yet measured, by design: the Collector's Kafka exporter and redaction behaviour
(WP7 Round 0) and the ClickHouse Java client (WP8 Round 0).

## 8. Sign-off

| Maintainer | Role | Date |
|:--|:--|:--|
| @merttemiz | Maintainer A | 06.10.2026 |
| @hasan4adnan | Maintainer B | 06.10.2026 |

Next: WP2 — Contracts Foundation, starting with its Round 0.
