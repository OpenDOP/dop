# ADR-015: Event identity and the idempotent write

- **Status:** **Draft.** Opened in WP1; closed by **WP9** (log ingestion pipeline).
- **Date:** 06.10.2026 (opened)
- **Deciders:** @merttemiz, @hasan4adnan

## Context

The ingestion pipeline is at-least-once at two points. The Collector may resend a batch to
Kafka, and core may reprocess a message whose offset was not committed. The second is under
DOP's control and is the common case: a restart, a consumer-group rebalance, a crash.

The acceptance criterion the master roadmap sets for this phase is that a message consumed
twice produces one visible row per record. Something must therefore give each record a stable
identity that survives reprocessing.

## Decision (draft — the proposal of Phase 1 roadmap §7.3, not yet closed)

`event_id` is a **deterministic value computed from the topic, the partition, the offset and
the record's ordinal within the decoded message.** Reprocessing a message yields the same
identifiers; two different records never share one.

The log table is a **`ReplacingMergeTree`** whose ordering key ends in `event_id`. Rows with an
identical key are collapsed at merge time, and the read path collapses those not yet merged.
The read technique — `FINAL`, an aggregating read, or something else — is chosen by measurement
in **WP8** (D1.11).

**What this guarantees.** A message consumed twice produces one visible row per record.

**What it does not.** A batch the Collector writes to Kafka twice arrives as two messages with
different offsets and is stored twice. The Kafka exporter's own delivery settings keep this
rare; it is named as a known limitation in the documentation.

## Alternatives considered

| Alternative | Why not |
|:------------|:--------|
| Identifier as a hash of the record's content | Two identical lines with the same timestamp are legitimate — tight loops, coarse clocks. A content hash would delete real data silently. |
| Identifier as a random UUID assigned at consumption | Reprocessing would create a second identifier and the write would not be idempotent at all. |
| Exactly-once through Kafka transactions | ClickHouse is not a participant in a Kafka transaction; the guarantee would end at the wrong boundary. |
| ClickHouse asynchronous inserts instead of an explicit buffer | The acknowledgement that allows the offset commit becomes harder to reason about; an explicit batch is a few lines. |

## Consequences

- The identifier depends on Kafka coordinates, so it is stable only as long as the topic is not
  recreated and partitions are not reassigned under a message. Both are documented operational
  constraints.
- Cross-message de-duplication is explicitly not offered, and the limitation is published
  rather than hidden.
- The read path pays a de-duplication cost on every query until a merge happens. How much is a
  WP8 measurement.
- Counts shown on the Overview are read without de-duplication and are documented as
  approximate; search results are exact.

## What closes this ADR

**WP9.** The status changes to Accepted when the ingestion pipeline exists and the
duplicate-elimination behaviour has been demonstrated against a reprocessed partition, with the
WP8 measurement of the read technique recorded here.
