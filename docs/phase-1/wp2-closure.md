# WP2 — Closure Record

**Work package:** WP2 — Contracts Foundation
**Phase:** 1 (v0.1.0 "Log Explorer")
**Opened:** Not recorded in the repository — **Closed:** Pending
**Owners:** both maintainers (@merttemiz, @hasan4adnan)
**Status:** Draft closure record; local implementation verified, final acceptance pending
**Rounds:** Read-only discovery; toolchain preparation; contract foundation; generation;
standalone consumption; interoperability; rename experiment; review and documentation;
contracts-only CI implementation

## 1. Result

The WP2 contract foundation is implemented in the working tree. One Protobuf message,
`dop.common.v1.EventMetadata`, is generated for Java and Python. Standalone consumers,
bidirectional golden-file tests and an isolated field-renaming experiment have been
verified locally on Windows. The contract conventions and contributor commands are
documented in [contracts/README.md](../../contracts/README.md).

The contracts-only GitHub Actions workflow is **IMPLEMENTED but NOT YET VERIFIED ON
GITHUB**. The feature branch has not been pushed and no WP2 pull request has been opened,
as reported by the maintainer. There is no GitHub run result to record. Local success is
not evidence of a clean Linux runner passing.

**WP2 is not fully closed.** Its remaining acceptance evidence is a successful real
GitHub CI run and final maintainer review/sign-off. Integration with the real core and
analytics applications is explicitly deferred to WP3 and WP4, respectively; it is not
unfinished WP2 implementation. Sections 3, 6 and 7 distinguish these responsibilities.

## 2. What was delivered

| Deliverable | Where |
|:--|:--|
| Buf v2 workspace and lint configuration | `contracts/buf.yaml` |
| Pinned Java/Python remote generation | `contracts/buf.gen.yaml` |
| Common metadata contract | `contracts/proto/dop/common/v1/event_metadata.proto` |
| Java 21 standalone library and verification tasks | `contracts/java/build.gradle.kts`, `settings.gradle.kts` |
| Gradle Wrapper 9.8.0 and Java dependency lock | `contracts/java/gradlew`, `gradlew.bat`, `gradle/wrapper/`, `gradle.lockfile` |
| Java consumption and interoperability verifiers | `contracts/java/src/test/java/io/github/opendop/contracts/EventMetadataConsumption.java`, `EventMetadataInterop.java` |
| Python verification environment and dependency lock | `contracts/python/pyproject.toml`, `uv.lock` |
| Python consumption tests and cross-language runner | `contracts/python/test_event_metadata.py`, `run_interop.py` |
| Two retained binary golden fixtures and binary attributes | `contracts/fixtures/java.bin`, `python.bin`, `.gitattributes` |
| Scoped generated/build/cache exclusions | `contracts/.gitignore` |
| Conventions, setup, commands, fixture values and rename evidence | `contracts/README.md` |
| Contracts-only Linux CI workflow | `.github/workflows/contracts-ci.yml` |

These are working-tree deliverables, not a claim that a WP2 commit or merge exists. In
particular, the two golden fixtures are intended for version control but are currently
untracked, along with the other new WP2 files. Generated source code is excluded.

### Contract shape and conventions

| Number | Field | Type |
|:--|:--|:--|
| 1 | `event_id` | `string` |
| 2 | `tenant_id` | `string` |
| 3 | `project_id` | `string` |
| 4 | `environment` | `string` |
| 5 | `occurred_at` | `google.protobuf.Timestamp` |
| 6 | `observed_at` | `google.protobuf.Timestamp` |

The schema uses `proto3`, the versioned package `dop.common.v1` and a matching directory
path. Filenames and fields use lower snake case; message names use PascalCase. Assigned
field numbers are permanent and must not be reused; removed numbers and names must be
reserved. Enumerations, when introduced, require an explicit zero `_UNSPECIFIED` value.
There is no enumeration or algorithm-specific state in this message.

The numbering groups identity/scope, environment and timestamps without speculative
reserved ranges. Identity assignment and ingestion behaviour are outside this schema.
`google/protobuf/timestamp.proto` is a well-known type available to Buf; the language
runtimes provide its Java/Python classes. No external Buf module, copied Google schema,
`buf.lock` or local `protoc` is required by the current design.

`buf.yaml` defines one module at `proto/`, with lint category `STANDARD`. Buf's built-in
formatter needs no separate configuration; `buf format --diff --exit-code` checks without
rewriting files. The required breaking-change gate starts at the `v0.1.0` tag with that
release as the baseline. Backward-compatible contract commitments begin in Phase 3,
as specified by architecture reference section 7.4.

### Generation and consumer environments

| Component | Pin / behaviour |
|:--|:--|
| Buf CLI | `1.73.0` |
| Java remote plugin | `buf.build/protocolbuffers/java:v35.1`, revision `1` |
| Python remote plugin | `buf.build/protocolbuffers/python:v35.1`, revision `1` |
| Java runtime | `com.google.protobuf:protobuf-java:4.35.1` |
| Python runtime | `protobuf==7.35.1` |
| JDK | Eclipse Temurin 21; verified local version `21.0.12.1+1` |
| Gradle | Wrapper `9.8.0`; no global installation required |
| Python / uv | `3.12.15` / `0.12.23` |

Generation writes Java to `contracts/build/generated/java/` and Python to
`contracts/build/generated/python/`. `clean: true` clears those configured output
directories before generation. Remote generation sends schema definitions to the Buf
service and requires network access. Generated code is neither hand-written nor committed.

The Java options are limited to `java_multiple_files = true` and package
`io.github.opendop.contracts.common.v1`. They preserve the Protobuf package and make
`EventMetadata` directly importable. The standalone build uses `java-library`, Java 21,
UTF-8 compilation and strict Gradle dependency locking. `compileJava` depends on the
Buf generation task, which also produces Python output. `consumptionTest` constructs,
serializes and parses the generated class, explicitly checking every field. Assertions
throw independently of Java's `-ea` option. The framework-based `test` task is disabled
but depends on this verifier, so `check` and `test` execute it without JUnit.

The Python directory is a verification environment, not an analytics application or
publishable package. Exact Python/uv requirements and `protobuf` are pinned in
`pyproject.toml`; `uv.lock` records artifact hashes. Standard-library `unittest` contains
two tests: all six field values and a binary round-trip. Its import path points to the
generated directory and fails explicitly when generation is missing.

### Golden-file interoperability

`run_interop.py` invokes the existing Gradle tasks and generated classes. Java writes a
fresh `build/interop/java.bin`, which Python reads; Python writes a fresh
`build/interop/python.bin`, which Java reads. Each reader also checks the retained fixture
from the other language. Both compare decoded field values with known constants rather
than assuming universally identical serialized bytes.

| Expected value | Java fixture | Python fixture |
|:--|:--|:--|
| Size | 106 bytes | 108 bytes |
| `event_id` | `event-java-golden-001` | `event-python-golden-001` |
| `tenant_id` | `tenant-örnek` | `tenant-örnek` |
| `project_id` | `project-東京` | `project-東京` |
| `environment` | `test-İstanbul-☃-🚀` | `test-İstanbul-☃-🚀` |
| `occurred_at.seconds` | `1700000000` | `1700000000` |
| `occurred_at.nanos` | `123456789` | `123456789` |
| `observed_at.seconds` | `1700000002` | `1700000002` |
| `observed_at.nanos` | `987654321` | `987654321` |

The tests check Timestamp presence, seconds and nanoseconds, and Unicode including a
supplementary character. Each reader rejects ten incorrect messages: four changed strings,
four changed timestamp components and two absent timestamps. An unexpectedly accepted
mutation fails verification. Missing fixtures, parse errors, field mismatches and failed
Gradle subprocesses also fail the runner. Normal runs never rewrite retained fixtures.
`fixtures/.gitattributes` marks `*.bin` as binary to prevent line-ending conversion.

## 3. Acceptance criteria

The four acceptance requirements below are drawn from the Phase 1 Reference Roadmap,
Revision 1, section 6.2, and were checked against the supplied roadmap PDF during final
document review. The repository implementation and actual local results provide the
verification evidence; CI evidence remains pending.

| Roadmap section 6.2 requirement | Implementation | Local verification | GitHub CI verification | Remaining WP2 evidence |
|:--|:--|:--|:--|:--|
| 1. `buf lint` and `buf format --diff` are clean | Implemented in the workspace and workflow | Passed; lint and non-mutating `buf format --diff --exit-code` both exit `0` | Pending; workflow has not run on GitHub | Record the real workflow result; local checks are already verified |
| 2. Java and Python builds both import the generated classes | Implemented by the standalone Java library/verifier and Python verification environment under `contracts/` | Passed; Java compiles and consumes the generated class, Python imports it and passes both unittest cases | Pending; both consumers are included in the workflow | GitHub run evidence; real application integration belongs to WP3/WP4, not this criterion |
| 3. The cross-language golden-file test passes in CI | Both directions implemented, with separate workflow steps | Passed in both directions, against fresh outputs and retained fixtures | **Pending — acceptance requirement not yet met in CI** | Successful real GitHub run, run URL, tested commit SHA and verification date |
| 4. A deliberately renamed field makes both consumers fail to compile or parse, demonstrated once with raw output | Completed isolated experiment; original consumers unchanged | Java compilation failed on missing accessors; Python message construction failed with `ValueError`; genuine output is retained in section 6 | Not part of the routine workflow; this is the required one-time demonstration | No further experiment is pending; maintainer review of the recorded evidence remains pending |

For requirement 4, the roadmap wording is interpreted according to the actual failure:
the rename breaks the generated source/API contract. Python fails during construction,
not binary parsing. The experiment does not imply that preserving the field number and
type necessarily breaks the wire format.

Supporting implementation and review checks remain documented separately:

| Supporting check | Status | Evidence |
|:--|:--|:--|
| Buf v2 workspace | Implemented; locally verified | One module at `proto/`; `STANDARD` lint |
| Metadata contract with the six required fields | Implemented; locally verified | Schema, generated classes and field-by-field tests |
| Java/Python generation with pinned compatible runtimes | Met locally | Remote generation exit `0`; consumer checks pass |
| Generated code excluded from Git | Verified locally | Scoped ignore rules and `git check-ignore` checks |
| Incorrect values cause verification failure | Met locally | Ten rejected cases in each reader |
| Contract conventions and contributor instructions | Implemented | `contracts/README.md` |
| Contracts checks execute together on clean Linux GitHub runner | Implemented, NOT YET VERIFIED ON GITHUB | Workflow exists; run URL, tested SHA, result and date pending |
| Final review and closure approval | Pending | Both maintainer sign-offs in section 8 |

The standalone consumers fulfill WP2's local generated-code consumption proof; they are
not the future core or analytics applications. WP3 owns the real Java core skeleton and
integration; WP4 owns the real Python analytics skeleton and integration. Those deferred
deliverables are not additional WP2 acceptance gates. Requirement 3 remains pending
regardless of local success. General CI and release automation remain assigned to WP6;
only the agreed contracts-specific CI exception is implemented here.

## 4. Decisions closed in WP2

The following implementation choices are settled for the delivered foundation. This table
does not create new accepted ADRs, change D1/D2 or substitute for final WP2 approval.

| Item | Recorded choice | Evidence |
|:--|:--|:--|
| Contract scope | Only `dop.common.v1.EventMetadata`; `NormalizedLogEvent` belongs to WP7 | Schema and contracts README |
| Generator distribution | Pinned remote Buf plugins; no local `protoc` | `buf.gen.yaml` and successful local generation |
| Consumer proof before application skeletons | Standalone Java library/verifier and Python unittest environment under `contracts/` | Build/test files; WP3/WP4 integration remains separate |
| Test dependencies | Explicit Java assertions and Python standard library; no additional test framework | Existing verifier implementations |
| Golden comparison | Known decoded values; no canonical byte-equality claim | Both language readers |
| Rename interpretation | Source-level incompatibility; binary incompatibility not inferred | Isolated experiment |
| Narrow CI sequencing exception | Both maintainers agreed to advance contracts-only Linux CI into WP2 | Maintainer instruction and contracts README; broader WP6 ownership unchanged |

D1.7 package identity and D1.8 tool pins remain in force. ADR-001 to ADR-005 are not
changed. ADR-015 and ADR-016 remain drafts owned by WP9 and WP10; a metadata shape does
not implement event identity, principal resolution or scope enforcement.

## 5. Deviations from the roadmaps

| Plan / expectation | Implementation or qualification | Reason / consequence |
|:--|:--|:--|
| General CI is delivered in WP6 | A single contracts-only workflow is implemented in WP2 by explicit agreement of both maintainers | Needed to obtain WP2 golden-test CI evidence; general changed-path CI, release automation and repository settings remain WP6 |
| WP2 verifies generated-code consumption; WP3/WP4 own the real applications | Standalone contract consumers verify compilation/import and serialization now | Preserves scope boundaries; future application integration is deferred WP3/WP4 work, not an additional WP2 acceptance gate |
| Original contracts README assigned `NormalizedLogEvent` to WP2 | README now assigns it to WP7; no such message was added | Corrected the misleading repository statement within the agreed WP2 scope |
| A field rename should demonstrate incompatibility | Unchanged consumers fail after generation from a temporary renamed schema | Field names affect generated APIs; keeping number and type does not necessarily break the binary wire format |

Deliberate exclusions: span, metric, pattern and anomaly contracts; schema registry; JSON
mappings; algorithm/domain logic; core/analytics application skeletons; Spring Boot,
Kafka, databases and containers. No general module CI, web CI, release publishing, DCO,
Conventional Commit or secret-scanning checks are added by this workflow. No branch
protection setting is changed. These exclusions do not amend later work-package ownership.

## 6. Verification and repository status

### Local commands and observed results

The evidence below comes from actual WP2 implementation/validation rounds, most recently
the contracts-only CI implementation round. It is not a new test run performed while
drafting this record. Local tools were Buf `1.73.0`, Temurin/Javac `21.0.12.1`, Python
`3.12.15` and uv `0.12.23` on Windows. Java compilation succeeded in the consumption
implementation round; subsequent runs reused up-to-date compilation outputs. No clean
Linux build is claimed.

With the pinned tools on PATH, the following are the local PowerShell commands, starting
at the repository root:

```powershell
Set-Location contracts
buf lint
buf format --diff --exit-code
buf generate
.\java\gradlew.bat --no-daemon --console=plain -p java check
Set-Location python
uv sync --frozen --python 3.12.15 --managed-python
uv run --frozen --no-sync python -m unittest -v
uv run --frozen --no-sync python run_interop.py --direction java-to-python
uv run --frozen --no-sync python run_interop.py --direction python-to-java
```

The Windows format check requires `diff.exe` on PATH. Relevant actual output excerpts:

```text
buf lint exit code: 0
buf format exit code: 0
buf generate exit code: 0
PASS: generated EventMetadata imported and all six fields verified
PASS: Java binary round-trip preserves strings and timestamp nanoseconds
BUILD SUCCESSFUL in 6s
Ran 2 tests in 0.000s
OK
PASS: Java -> Python: build/interop/java.bin; all six fields, seconds and nanos
PASS: Java -> Python: fixtures/java.bin; all six fields, seconds and nanos
PASS: Python verifier rejected all 10 incorrect-value/presence cases
PASS: Python -> Java: build/interop/python.bin; all six fields, seconds and nanos
PASS: Python -> Java: fixtures/python.bin; all six fields, seconds and nanos
PASS: Java verifier rejected all 10 incorrect-value/presence cases
git diff --check exit code: 0
```

### Completed isolated rename experiment

The experiment recorded in the contracts README on 19 October 2026 changed only
`string event_id = 1;` to `string event_reference = 1;` in temporary files. Number `1`
and type `string` were unchanged. The pinned generators produced temporary Java/Python
classes, with `event_reference` present and the original name/accessors absent.
Byte-for-byte unchanged copies of the original consumption tests were used.

The experimental Java generated classes compiled independently with the pinned runtime.
Compiling the unchanged consumer against them produced these genuine diagnostics:

```text
  symbol:   method setEventId(String)
  location: class Builder
  symbol:   method getEventId()
  location: variable message of type EventMetadata
2 errors
Unchanged Java consumer javac exit code: 1
```

The experimental Python module imported and worked with its renamed field and pinned
runtime. The unchanged consumer instead failed in both unittest cases:

```text
ValueError: Protocol message EventMetadata has no "event_id" field.
Ran 2 tests in 0.001s
FAILED (errors=2)
Unchanged Python consumer exit code: 1
```

These were field-name-related source/API failures, not dependency or environment failures.
They do not demonstrate that binary parsing fails after a field-name-only change.
The authoritative schema, original generated files and retained fixtures were never
overwritten. Temporary experimental outputs were removed. Original consumer tests,
both golden directions and Buf checks passed afterward; schema/fixture hashes matched
the previously recorded values. No restoration of authoritative files was necessary.

### Integrity and Git hygiene

| Artifact | SHA-256 |
|:--|:--|
| Gradle 9.8.0 binary distribution, pinned in Wrapper/build configuration | `bafd5ce9cfaea0fbccfdc8439a1ac42fbd4cd9c89dc9a988228d8a2639a58e6c` |
| Wrapper JAR, locally measured and matching D2 | `238e777fcddd7e34f9708186085def2abd6e08e658505b38718d79d74c21abd5` |
| Retained Java fixture | `51999e5a6311db3f0bb8312696dbfffbf654bd4dbc6c25fa4870c5935c7eb3e1` |
| Retained Python fixture | `bd233b870c7cbce9c2a8bedc6cdd75270766f8677fe69a4bb70f9a8e3dc08470` |

The Wrapper was bootstrapped from a checksum-verified distribution in an earlier WP2
round; its bootstrap procedure is documented. Normal development uses the included
Wrapper. Java dependencies use strict version locking; Python uses exact requirements
and a frozen lock with artifact hashes. No global Gradle or local `protoc` is required.

Ignore checks cover `build/generated/`, `build/interop/`, Java build/cache directories,
the Python virtual environment and bytecode cache. Wrapper files, locks, handwritten
tests and the two retained fixtures are intended to be tracked. They are not ignored.
Whitespace checks covered tracked modifications and untracked text files separately.

The current branch is `feat/wp2-contracts-foundation`. Before this record was added,
Git reported one modified tracked file (`contracts/README.md`) and 21 untracked WP2
files, including the workflow. This record is an additional untracked document. Existing
work has not been staged or discarded. No WP2 commit, PR or merge identifier is asserted.

### Contracts-only workflow and pending GitHub evidence

The workflow is **IMPLEMENTED but NOT YET VERIFIED ON GITHUB**. It triggers on pull
requests targeting `main` when `contracts/**` or the workflow itself changes. One job,
`Contracts (Linux)`, uses `ubuntu-24.04`, Bash and a 20-minute timeout. Default command
working directory is `contracts/`, with Python steps using `contracts/python/`.

| Action | Version label | Verified pinned commit |
|:--|:--|:--|
| `actions/checkout` | `v7.0.1` | `3d3c42e5aac5ba805825da76410c181273ba90b1` |
| `actions/setup-java` | `v6.1.0` | `b843ebf9b913146b38da2eaa7aeddf76074ae1ad` |
| `bufbuild/buf-action` | `v1.6.0` | `85aebf73123b5c15fd5528aaecbf9129cddf7fa7` |
| `astral-sh/setup-uv` | `v10.3.0` | `1c37ad07a6a961277cf70c0d37d6f313000f5884` |

Release tags and action input metadata were verified through official GitHub sources in
the CI implementation round. The token has only `contents: read`; checkout does not
persist credentials. Buf is setup-only with PR comments disabled; uv cache uploads are
disabled. No repository secret, write permission, `pull_request_target`, publication,
matrix or service container is required.

The workflow's sequence is:

1. Checkout, set up Temurin `21.0.12.1+1`, Buf `1.73.0` and uv `0.12.23`.
2. Report Java/Javac/uv/Wrapper versions and check the exact Buf version.
3. Run `buf lint`, `buf format --diff --exit-code` and `buf generate`.
4. Run `sh java/gradlew --no-daemon --console=plain -p java check`.
5. Install uv-managed Python `3.12.15`, sync with `--frozen --python 3.12.15 --managed-python`, and report Python's version.
6. Run the existing Python unittest suite.
7. Run `run_interop.py --direction java-to-python` through the frozen uv environment.
8. Run `run_interop.py --direction python-to-java` through the frozen uv environment.

Local workflow validation passed with checksum-verified temporary `actionlint 1.7.12`;
all nine `run` steps passed Git Bash syntax checks. These are syntax checks, not Linux
execution. The POSIX Wrapper has LF line endings. It is invoked with `sh`, so no
executable bit is required; the Python runner selects this path on non-Windows hosts.
The local Docker Linux engine was unavailable and no general-purpose Linux WSL
distribution was available. Network access to Buf and first-time tool/dependency
downloads remains a requirement of a fresh runner.

**Pending evidence — complete only after observing the real GitHub run:**

| Evidence field | Value |
|:--|:--|
| GitHub Actions run URL | **Pending** |
| Tested commit SHA | **Pending** |
| Actual CI result | **Pending — workflow has not executed on GitHub** |
| Date of verification | **Pending** |

After a maintainer commits/pushes the branch and opens a PR targeting `main`, inspect
the actual `Contracts CI` run, including both golden steps. Record its URL, tested SHA,
result and verification date here. Investigate failures instead of retrying until green.
Do not fill these fields from local results or action dependency commit SHAs.

## 7. Carried forward

| Item | Owner / acts in |
|:--|:--|
| Obtain real contracts-only GitHub Actions evidence, including both golden directions on Linux | WP2-specific exception; both maintainers review the run |
| Integrate generated Java contracts into the real core application build | WP3 |
| Integrate generated Python contracts into the real analytics application build | WP4 |
| General changed-path CI, application/web CI, release automation and repository status-check settings | WP6; not delivered by this exception |
| `dop.telemetry.v1.NormalizedLogEvent` and its ingestion-facing contract work | WP7 |
| Event identity implementation and acceptance of draft ADR-015 | WP9 |
| Principal/scope implementation and acceptance of draft ADR-016 | WP10 |
| Required Buf breaking gate using `v0.1.0` as baseline | At the `v0.1.0` tag |
| Review the section 6.2 acceptance evidence and approve the final closure after CI passes | Both maintainers |
| Confirm any still-open WP1 repository-settings checklist items | Maintainers; no GitHub settings were re-verified in WP2 |

The first row is remaining WP2 acceptance evidence; WP3/WP4 application integration and
general WP6 automation are deferred work belonging to those packages. Later application
integration must reuse the same schema and generation pins rather than treating standalone
test environments as production skeletons. Passing the first CI run does not complete
those later deliverables or provide maintainer approval, but WP2 closure does not require
the future application skeletons to be implemented early.

## 8. Sign-off

**Pending.** This is an evidence record prepared for review, not a signed closure.

| Maintainer | Role | Approval | Date |
|:--|:--|:--|:--|
| @merttemiz | Maintainer A | Pending | Pending |
| @hasan4adnan | Maintainer B | Pending | Pending |

Both maintainers must review the contract change and the actual CI evidence against the
four WP2 requirements, and record their own approval/date before a final closure date is
entered. Deferred WP3/WP4/WP6 deliverables retain their own work-package ownership.
No signature, approval, completion
date, CI run URL, WP2 commit SHA, PR number or merge commit is fabricated by this record.
