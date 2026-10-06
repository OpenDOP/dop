# ADR-001: Licence and contribution sign-off

- **Status:** Accepted
- **Date:** 05.10.2026
- **Deciders:** @merttemiz, @hasan4adnan

## Context

DOP is published as an open-source product whose priorities are community adoption and use
inside companies. Two things must be settled before the first external contribution arrives,
because relicensing afterwards is in practice very hard: which licence the code carries, and
how a contributor certifies that they may submit it.

The repository is public from the first day, so "we will decide when it is ready" is not
available.

## Decision

DOP is licensed under **Apache-2.0**, unmodified, in `LICENSE` at the repository root.

Contributions are certified with the **Developer Certificate of Origin**: every commit carries
a `Signed-off-by` line, produced by `git commit -s`. **There is no Contributor Licence
Agreement.**

## Alternatives considered

| Alternative | Why not |
|:------------|:--------|
| AGPL-3.0 | Makes it harder for a "hosted" competitor to appear later, but measurably lowers self-hosted adoption inside companies, which is the group DOP is built for. |
| MIT or BSD | Shorter, but carries no patent grant. Apache-2.0 is the lowest-friction choice that corporate legal departments already recognise, and it matches the CNCF ecosystem DOP sits in. |
| A CLA instead of the DCO | Adds a signing step before a first contribution and a legal entity to administer it. The DCO achieves the needed certification with one line in a commit message. |

## Consequences

- The patent grant and the recognised text remove a common blocker for corporate users.
- Relicensing later would need the agreement of every contributor; this choice is effectively
  permanent, which is why it is made before the first external contribution.
- Keeping no CLA means the project never holds assignment of contributor copyright. A future
  relicense or a commercial edition built on contributed code is therefore off the table, which
  the maintainers accept (see architecture reference §19.3).
- Every contributor must configure a real name and e-mail in Git. The sign-off is enforced by
  hand until the DCO check lands in CI (WP6).
- `NOTICE` carries the project name and the copyright line.
