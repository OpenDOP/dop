<!--
The pull-request title must be a Conventional Commit, for example:
  feat(core): add keyset pagination to the log search endpoint
  docs(adr): record the Spring Boot 4.1.x decision in ADR-004
-->

## What and why

<!-- What this changes, and the problem it solves. Link the issue or Discussion if there is one. -->

## How it was tested

<!--
Commands run and what they showed. If something could not be tested, say so.
No "retry until green": a flaky run is investigated, not re-run.
-->

## Checklist

- [ ] The title is a **Conventional Commit** (`type(scope): description`).
- [ ] **Docs updated?** Every user-visible change carries its documentation page in this same
      pull request — or this change has no user-visible effect.
- [ ] Every commit is **signed off** (`git commit -s`), per the DCO. No CLA is required.
- [ ] No secret, credential, real telemetry or personal data is added anywhere, including
      fixtures, logs and screenshots.
- [ ] A decision that others will have to live with is recorded as an ADR in `docs/adr/`.
- [ ] Contract or security-relevant code? Then **both maintainers** review this.

## Anything the reviewer should know

<!-- Trade-offs taken, things deliberately left out, follow-up work. Delete if empty. -->
