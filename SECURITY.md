# Security Policy

## Reporting a vulnerability

Report security vulnerabilities **privately**, through GitHub's private vulnerability
reporting:

**<https://github.com/OpenDOP/dop/security/advisories/new>**

**Do not open a public issue, a pull request or a Discussion thread for a vulnerability.** A
public report puts every installation at risk before a fix exists. If the form above is
unavailable to you, say so in a Discussion without any technical detail and a maintainer will
arrange a private channel.

Please include what you can: the affected component, the version or commit, what an attacker
gains, and the smallest reproduction you have. Never include real telemetry, personal data,
credentials or tokens in a report.

Both maintainers read these reports. The project is two people working to a public roadmap;
there is no paid on-call rotation and no guaranteed response time. We will acknowledge a report
as soon as we see it and keep you informed while we work on it. We will credit you in the
advisory unless you ask us not to.

There is no bug-bounty programme.

## Supported versions

**No release exists yet.** The first release, v0.1.0, is targeted for 11.12.2026. Until then
there is no supported version and no released artefact to patch; reports against the `main`
branch are welcome all the same.

| Version | Supported |
|:--------|:----------|
| none released yet | — |

The long-term policy, which takes effect at v1.0, is security patches for the last two minor
versions. This table is updated when the first release exists.

## Releases before v0.5 are unauthenticated

**Releases v0.1 through v0.4 have no authentication, no users, no roles and no API tokens.**
Anyone who can reach the interface or the API can read all stored telemetry. This is a stated
property of those releases, not a defect, and a report that only says "the API needs no
credentials" will be closed with a link to this section.

What is in scope for those releases:

- The default Compose profile publishing a port beyond `127.0.0.1`, or publishing a data-store
  port at all, without an explicit setting and a documented warning.
- A sender being able to choose the tenant, project or event identifier of the data it sends.
- A query that escapes its scope, or a path that reaches the data stores without going through
  the central query builder.
- Secrets, credentials or real telemetry committed to the repository or baked into an image.
- Injection, deserialisation and resource-exhaustion defects in the ingestion and query paths.
- Vulnerabilities in a pinned dependency or base image that we ship.

Authentication, RBAC, API tokens, OIDC, tenant isolation and audit arrive in **v0.5**
(Phase 5). Do not expose a v0.1–v0.4 installation to an untrusted network.
