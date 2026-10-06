# ADR-016: Principal and scope

- **Status:** **Draft.** Opened in WP1; closed by **WP10** (query API).
- **Date:** 06.10.2026 (opened)
- **Deciders:** @merttemiz, @hasan4adnan

## Context

v0.1 has no authentication, no users and one tenant. Multi-tenancy, roles and tokens arrive in
Phase 5, six phases later.

The expensive mistake available here is to write v0.1 as if a single tenant were the only case:
handlers that read a tenant from a request parameter, queries assembled ad hoc, and a scope
that has to be retrofitted into every endpoint and every query in Phase 5. The master roadmap
names avoiding that retrofit as the reason to build the abstraction now, when it costs a few
classes.

## Decision (draft — the proposal of Phase 1 roadmap §7.5, not yet closed)

**Principal.** A value object: who is asking, for which tenant and project, with which
permissions. Every handler receives one. In v0.1 a single resolver returns one built-in
principal with the default tenant and project. **Handlers never construct a principal and never
read tenant or project from a request parameter.**

**Scope.** Derived from the principal. The central query builder's entry point takes a scope as
a **non-optional** argument; **there is no overload without it.**

**Identity comes from the platform, not from the payload.** Tenant, project and event
identifier are assigned by core. Nothing a sender writes into an event can change whose data it
is.

Every ClickHouse read goes through the central query builder. There is no second read path, and
the live tail is a read through the same builder, not a separate delivery mechanism.

## Alternatives considered

| Alternative | Why not |
|:------------|:--------|
| Add the abstraction in Phase 5, when it is actually needed | It would have to be retrofitted into every handler and every query written in Phases 1 to 4. The master roadmap sizes that as the larger cost. |
| Tenant as a request parameter with a default | A sender or a caller could then choose whose data it reads or writes. Identity must not come from the payload. |
| A scope argument with an overload that omits it | The overload becomes the one everyone calls, and the guarantee is gone. Non-optional is the whole mechanism. |
| Row-level security in ClickHouse instead | Pushes the guarantee into a store whose access-control model DOP does not control, and gives no answer for PostgreSQL or for the API surface. |

## Consequences

- A few classes and one non-optional argument exist from the first endpoint, for a product that
  has one tenant. That is the accepted cost.
- Phase 5 adds real principals and real scopes behind an interface that already exists, rather
  than changing every call site.
- An architecture test must enforce that no ClickHouse read bypasses the query builder,
  otherwise the guarantee decays silently.
- A v0.1 installation is still fully readable by anyone who can reach it: this ADR is about not
  creating debt, **not** about securing v0.1. See [SECURITY.md](../../SECURITY.md).

## What closes this ADR

**WP10.** The status changes to Accepted when the query API exists, the principal resolver and
the scope-carrying query builder are in place, and the architecture test that forbids a bypass
is green.
