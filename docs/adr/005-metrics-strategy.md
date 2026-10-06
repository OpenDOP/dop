# ADR-005: Metrics strategy — decision deferred to the start of Phase 6

- **Status:** Accepted as a record. **The decision itself is due at the start of Phase 6.**
- **Date:** 05.10.2026
- **Deciders:** @merttemiz, @hasan4adnan

## Context

There is an open question about metrics: should DOP **store** metrics itself, or should it
**read** them from an existing Prometheus installation? Both are defensible. Storing them makes
DOP complete and makes metric anomalies first-class evidence; reading them avoids duplicating a
mature ecosystem and keeps DOP out of a competition with Grafana it has already said it is not
in.

The answer depends on something that does not exist yet: what users of v0.1 to v0.4 actually
do with DOP next to the Prometheus and Grafana they already run.

Metrics are deliberately the last signal DOP adds. They are not needed for logs (Phase 1),
traces (Phase 2), pattern mining (Phase 3) or incident correlation (Phase 4). But a v1.0 with
no metrics at all would read as an incomplete observability platform, so the question cannot be
left open indefinitely either.

## Decision

**No metrics decision is taken now.** This ADR exists so that the question is recorded, owned
and scheduled rather than forgotten.

- User feedback from Phases 1 to 4 is collected as it arrives, specifically on what users do
  with their existing Prometheus and Grafana alongside DOP.
- The decision is **made at the start of Phase 6** and recorded by superseding this ADR.
- The master roadmap's own proposal is written in its Phase 6 section and is the starting
  point, not the conclusion.

What is already fixed, independent of the outcome: **DOP is not a Grafana.** There is no
dashboard editor and no PromQL-like query language in any case. Metrics exist to complete the
picture and to serve as incident evidence.

## Alternatives considered

| Alternative | Why not |
|:------------|:--------|
| Decide now to store metrics in ClickHouse | Commits the storage schema, the ingestion path and the cardinality controls before a single user has said what they need. The most expensive thing to get wrong. |
| Decide now to read from Prometheus only | Makes a Prometheus installation a hard dependency of DOP's incident evidence, which contradicts "one command on a laptop". |
| Leave the question unwritten until Phase 6 | In a two-person team the most expensive thing is a decision left open with no owner and no date. Writing it down with a due date is the cheap half of deciding. |

## Consequences

- Phases 1 to 4 are built without any metrics assumption. No table, contract or endpoint is
  shaped by a guess about metrics.
- Someone must actually collect the feedback. That is part of the community work of each phase,
  not a separate task that appears in Phase 6.
- If the decision slips past the start of Phase 6, Phase 6 cannot start. That is the intended
  forcing function.
- This ADR is superseded, not edited, when the decision is taken.
