# Contributing to DOP

Thank you for looking at DOP this early. The repository is in Phase 1 and most of it is still
empty; see [docs/roadmap.md](docs/roadmap.md) for what is being built and when.

By participating you agree to the [Code of Conduct](CODE_OF_CONDUCT.md).

## Where to ask

Use **[GitHub Discussions](https://github.com/OpenDOP/dop/discussions)** for questions, ideas,
design debate and "is this a bug?". Open an **issue** only for a confirmed defect or a concrete
piece of work. Never open a public issue for a security vulnerability — see
[SECURITY.md](SECURITY.md).

A request that does not belong to the current phase is recorded as an issue labelled `backlog`.
It is not refused; it is scheduled.

## Branches and pull requests

- `main` is protected. Every change reaches it through a pull request.
- Branch from `main`. Name the branch after the work, for example `wp7-collector-pipeline`
  or `fix-tail-cursor`.
- Keep a pull request to one subject. A large change is easier to review split into several.
- Rebase on `main` rather than merging `main` into your branch.
- CI must be green before a merge. There is no "retry until green": a flaky run is
  investigated, not re-run.
- **No user-visible feature is merged without its documentation page in the same pull
  request.**

## Review

- Every pull request is reviewed by a maintainer who did not write it, normally within
  24 hours.
- **Contract code and security-relevant code are reviewed by both maintainers.** "Contract
  code" means anything under `contracts/` and the generated-code contract surface;
  "security-relevant" means the principal and scope path, the query builder, redaction,
  authentication when it arrives, and anything touching network exposure or secrets.
- Ownership of a directory means the last word in review, not exclusive access. See
  [CODEOWNERS](.github/CODEOWNERS).

## Commit messages — Conventional Commits

Commit messages and pull-request titles follow
[Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). The release
automation reads them to produce the changelog and the version.

```
<type>(<optional scope>): <description>
```

Types in use: `feat`, `fix`, `docs`, `refactor`, `test`, `build`, `ci`, `chore`, `perf`.
Scopes are the top-level modules: `contracts`, `core`, `analytics`, `web`, `infrastructure`,
`examples`, `scripts`, `docs`. A breaking change is marked with `!` after the type or scope
and explained in the commit body.

```
feat(core): add keyset pagination to the log search endpoint
docs(adr): record the Spring Boot 4.1.x decision in ADR-004
```

## Sign-off — DCO, not a CLA

DOP uses the [Developer Certificate of Origin](https://developercertificate.org/). There is no
Contributor Licence Agreement. You certify that you wrote the contribution or have the right to
submit it under Apache-2.0, by adding a `Signed-off-by` line to every commit.

Git writes that line for you with `-s`:

```
git commit -s -m "fix(web): keep the detail drawer open across a filter change"
```

The line uses the name and e-mail in your Git configuration and must be a real identity:

```
Signed-off-by: Jane Doe <jane@example.org>
```

Set them once:

```
git config --global user.name "Jane Doe"
git config --global user.email "jane@example.org"
```

To sign off commits you have already made:

```
git rebase --signoff main          # a range of commits
git commit --amend -s --no-edit    # the most recent commit
```

Every commit in a pull request needs the line. A DCO check enforces this once CI exists
(WP6); until then the maintainers check it by hand.

## AI coding assistants

AI coding assistants are used throughout this project, under rules the repository enforces
(architecture reference §19.4):

- **The diff and the test output are always read by a human.** Contract code and
  security-relevant code are reviewed by both maintainers.
- **The assistant never commits or pushes.** Every commit is made and signed off by a person,
  who takes responsibility for it under the DCO.
- **Work proceeds in rounds:** a read-only discovery first, one prompt at a time, and every
  round reports the assumptions that turned out wrong and any overengineering it found.
- The standing rules live in [CLAUDE.md](CLAUDE.md) at the repository root.

You do not have to disclose assistant use beyond this: the sign-off already states that you
take responsibility for the contribution.

## Local setup

**Not written yet, on purpose.** There is nothing to build: `contracts/`, `core/`,
`analytics/`, `web/` and `infrastructure/` contain no build files at the time of writing.

This section is filled in as the modules land, and each entry arrives in the pull request that
makes it true:

| What | Arrives with |
|:-----|:-------------|
| Buf workspace, `buf generate` | WP2 |
| Gradle build for `core`, JDK requirement | WP3 |
| `uv` setup for `analytics`, `npm ci` for `web` | WP4 |
| `docker compose` development stack and `make` targets | WP5 |
| Running the test gate locally | WP6 |

One rule already holds: **reproducible installs from lock files only** — `npm ci`,
`uv sync --frozen`, Gradle dependency locking. Never an unpinned install.
