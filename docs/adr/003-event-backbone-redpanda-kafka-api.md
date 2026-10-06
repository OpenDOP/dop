# ADR-003: Event backbone — Redpanda by default, written against the Kafka API

- **Status:** Accepted
- **Date:** 05.10.2026
- **Deciders:** @merttemiz, @hasan4adnan

## Context

Kafka is part of the product: it is the buffer between ingestion and storage and the transport
between core and analytics. It is also the component that small teams fear operating, and the
largest single memory cost in a single-machine bundle that has to fit beside a browser and an
IDE on 8 GB of RAM.

A user running DOP on a server may already have a Kafka cluster and will not want a second one.

## Decision

The product is written **against the Kafka API** — Spring for Apache Kafka on the core side —
and never against a vendor-specific feature.

The default Compose profile runs **Redpanda** in its single-node development mode: one binary,
no JVM, low memory. **Kafka in KRaft mode is offered as a separate Compose profile.** On a
server or in Kubernetes the user may point DOP at their own Kafka.

The test scope of the Kafka profile is narrowed and fixed in **WP5**: it is a supported
configuration, not a second matrix that every change is tested against.

## Alternatives considered

| Alternative | Why not |
|:------------|:--------|
| Kafka KRaft as the default | A JVM broker is the heaviest component in the bundle and is exactly the operational fear the decision is meant to remove. |
| Redpanda only, no Kafka profile | Users with an existing Kafka cluster, and anyone who distrusts a single-vendor dependency, are excluded for no architectural gain. |
| No broker at all — the Collector's ClickHouse exporter writing directly | No buffer, a schema DOP does not own, and no place to assign tenant or to feed analytics. Faster to demo and would have to be removed in Phase 3. |
| NATS, Pulsar or Redis Streams | None is Kafka-API compatible, so the product would be written against a backbone the user cannot substitute. |

## Consequences

- Code, configuration and documentation speak Kafka. Swapping the broker is a Compose-profile
  change, not a code change.
- Two broker configurations exist, so topic creation, retention settings and the Collector's
  Kafka exporter must work on both. WP5 fixes how much of that is tested.
- No Redpanda-specific feature may be used, including its admin API and its schema registry,
  without superseding this ADR.
- Round 0 of WP1 measured Redpanda v26.2.2 at 585 MiB idle in `dev-container` mode with
  `--smp 1`. The pin is `v26.2.3` and is re-measured in WP5; a production-mode Redpanda is
  substantially larger and is not what the quickstart runs.
