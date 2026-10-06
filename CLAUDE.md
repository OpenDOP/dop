# CLAUDE.md — standing rules for AI coding assistants in this repository

These are the house rules for any AI coding assistant working on DOP. They come from the
Phase 1 roadmap §19 and the architecture reference §19.4, and they apply to every round, in
every module, without being repeated in each prompt.

## Project identity (decision D1.7, closed 05.10.2026)

| Item | Value |
|:-----|:------|
| GitHub organisation and repository | `OpenDOP/dop` |
| Image registry namespace | `ghcr.io/opendop` — images `dop-core`, `dop-analytics`, `dop-web` |
| Java base package | `io.github.opendop` |
| Protobuf package root | `dop.*` |
| Published package names | `opendop` / `@opendop/*` — the bare name `dop` is taken on PyPI and npm |
| Licence | Apache-2.0 with DCO sign-off, no CLA (ADR-001) |

## Precedence on conflict

```
code  >  accepted ADRs  >  Phase 1 roadmap  >  master roadmap  >  discovery reports  >  conversation
```

When a source lower in this order contradicts a higher one, the higher one wins and **the
contradiction is written into the round's report**, not silently resolved.

## Delivery pattern (Phase 1 roadmap §6)

- **Every work package starts with a read-only discovery — Round 0 — and stops after it.**
  In a repository that is still empty, Round 0 measures the outside world instead of the code:
  each pinned upstream component is run locally and its real behaviour — option names,
  defaults, encodings, limits — is recorded before a design depends on it.
- **One prompt at a time.** Contracts, core, analytics, web and infrastructure are separate
  rounds.
- **Never commit, never push.** No `git add`, `git commit`, `git push`, no branch, no tag, and
  no change to a GitHub setting. The assistant writes files in the working tree; a maintainer
  reads the diff, commits it and signs it off.
- **Every round reports the assumptions that turned out wrong** and any overengineering it
  found — in its own output too. "None found" is allowed only after checking.
- Targeted test runs during a round; the full gate at its close. **No "retry until green"**: a
  failing or flaky run is investigated, not re-run.

## Standing rules (Phase 1 roadmap §19)

- **Reproducible installs from lock files only** — `npm ci`, `uv sync --frozen`, Gradle
  dependency locking. Never an unpinned install.
- **Never print a secret value.** Never place real telemetry or personal data in code,
  fixtures, logs, screenshots or documents.
- **The simplest working solution.** No new abstraction, generic structure, layer or
  dependency without a concrete present need — and a dependency only after asking.
- **New API endpoint** = principal + scope + server-side limits by default.
  **New ClickHouse read** = through the central query builder by default.
  **New table** = by migration by default.
  **New event** = a contract in `contracts/` by default.
- **No user-visible feature without its documentation page in the same pull request.**
- **Do not invent decisions.** Where the sources are silent or contradict each other, write the
  gap into the report, not a guess into a file.
- **Describe only what exists or is decided.** Nothing in this repository may claim that a
  feature works today.

## Review obligations

The diff and the test output are always read by a human. Contract code and security-relevant
code are reviewed by both maintainers. Every commit is made and signed off by a person.

## Where the decisions live

- **[docs/adr/](docs/adr/)** — Architecture Decision Records. Read these before proposing
  anything they already settle; `docs/adr/template.md` is the format for a new one.
- **[docs/architecture/architecture-reference.md](docs/architecture/architecture-reference.md)**
  — the architecture and technical reference. It is the authoritative copy and is not edited in
  passing; a change to it is its own pull request with a reason.
- **[docs/roadmap.md](docs/roadmap.md)** — the eight phases and what each must prove.
- **[docs/phase-1/decision-record.md](docs/phase-1/decision-record.md)** — the Phase 1 decision
  items, their status and their owners.
- **[docs/phase-1/upstream-reality.md](docs/phase-1/upstream-reality.md)** — measured upstream
  versions and defaults, and what has not been measured yet.

## Maintainers

| Maintainer | Scope |
|:-----------|:------|
| @merttemiz — platform | `contracts/`, `core/`, `infrastructure/`, the data stores |
| @hasan4adnan — intelligence and experience | `analytics/`, `web/`, `examples/`, `scripts/`, CI |

Documentation, ADRs, releases and community are shared. Ownership means the last word in
review, not exclusive access.
