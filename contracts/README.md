# contracts/

The Protobuf contracts and the Buf workspace. Every event that crosses a process boundary is
defined here first; the Protobuf package root is `dop.*`.

- **Owner:** @merttemiz (platform)
- **Work packages:** WP2 establishes `dop.common.v1.EventMetadata` and the contract tooling.
  WP7 adds `dop.telemetry.v1.NormalizedLogEvent`. Together they contribute to deliverable D4.

## Current scope

This directory contains a Buf workspace with one module rooted at `proto/` and one message:
`dop.common.v1.EventMetadata`. Java and Python generation is configured in `buf.gen.yaml`.
Standalone Java and Python consumption checks and a bidirectional golden-file test live in
`java/` and `python/`. Integration with the future core/analytics builds is not implemented
yet. Generated code must be produced by the build, never hand-written or committed.

## EventMetadata

Source: `proto/dop/common/v1/event_metadata.proto`.

| Field number | Field | Type | Meaning |
|:--|:--|:--|:--|
| 1 | `event_id` | `string` | Identifier assigned by DOP, never taken from the sender |
| 2 | `tenant_id` | `string` | Tenant assigned by core |
| 3 | `project_id` | `string` | Project assigned by core |
| 4 | `environment` | `string` | Deployment environment from the resource attributes |
| 5 | `occurred_at` | `google.protobuf.Timestamp` | When the event happened at the source |
| 6 | `observed_at` | `google.protobuf.Timestamp` | When the telemetry pipeline observed it |

The numbering groups identity and scope (1–3), environment (4), and event times (5–6).
All six fields use the compact 1–15 field-number range. These assignments are permanent;
future fields receive unused numbers. No speculative ranges are reserved.

The message defines the data shape only. Identity assignment, timestamp fallback rules and
other ingestion behaviour belong to the ingestion implementation, not to this contract.

## Contract conventions

- Use `proto3` syntax and versioned packages such as `dop.common.v1`. The directory relative
  to `proto/` mirrors the package: `dop/common/v1/`.
- Use `lower_snake_case.proto` filenames, `PascalCase` message names and `lower_snake_case`
  field names.
- Never renumber or reuse an assigned field number. When removing a field, reserve both its
  number and its name with `reserved`; reserve the entire range when removing a field range.
- Represent identifiers as strings and timestamps as `google.protobuf.Timestamp`.
- Enumerations have an explicit zero value ending in `_UNSPECIFIED`. Use enumerations for
  values the code reasons about and plain strings for values senders invent.
- Contracts describe exchanged data, never algorithm-internal state.
- Generate Java and Python code from the same source; do not commit generated code.

## Contributor setup

Install the pinned tools and make `java`, `javac`, `buf` and `uv` available on PATH:

| Tool | Required version |
|:--|:--|
| Eclipse Temurin JDK | Java 21; D2 pin `21.0.12.1` |
| Buf CLI | `1.73.0` |
| Python | `3.12.15` |
| uv | `0.12.23` |
| Gradle | Wrapper `9.8.0`, included in `java/`; do not install globally |

Set `JAVA_HOME` to the JDK 21 installation directory, not its `bin` directory, and make its
`bin` directory available on PATH. Check `java -version`, `javac -version`, `buf --version`
and `uv --version` before starting. No private activation script is required.

`uv sync --frozen --python 3.12.15` prepares the local verification environment using
`uv.lock` and the exact Python version. Python may be installed separately or managed by uv;
uv may download it if it is missing. Check it with `uv run --frozen --no-sync python --version`
from `contracts/python/` after syncing.

Network access is required for remote Buf generation and first-time downloads of the Gradle
distribution, Maven/PyPI dependencies or managed Python. The Wrapper downloads Gradle and
checks its pinned distribution checksum. The Windows PowerShell commands in the sections
below start at the repository root unless stated otherwise.

For Linux, the complete local verification sequence from the repository root is:

```sh
set -eu
cd contracts
buf --version
buf lint
buf format --diff --exit-code
buf generate
sh java/gradlew --no-daemon --console=plain -p java check
cd python
uv sync --frozen --python 3.12.15
uv run --frozen --no-sync python -m unittest -v
uv run --frozen --no-sync python run_interop.py
```

This is the intended Linux invocation; real Linux runner evidence is still pending. The
contracts-only workflow below brings this verification forward from WP6 by maintainer
agreement. A standard Linux shell environment must provide `sh`, `diff`,
`xargs` and the usual Unix utilities used by the Wrapper.

## Buf checks

Use Buf CLI **1.73.0**, pinned in `docs/phase-1/upstream-reality.md`. `buf.yaml` uses the v2
configuration format for the workspace and module, with the built-in `STANDARD` lint rules.
Buf has a built-in formatter; it needs no separate formatter configuration.

From the repository root, in Windows PowerShell with the tools on PATH:

```powershell
Set-Location contracts
buf --version
buf lint
buf format --diff --exit-code
```

The format check does not rewrite files and returns a nonzero exit code for any formatting
difference. To apply formatting intentionally, run `buf format --write` and repeat the check.
On Windows, make `diff.exe` from Git for Windows available on PATH for `buf format --diff`.
Check its availability with `Get-Command diff.exe`; no personal installation path is assumed.

`google/protobuf/timestamp.proto` is a Protobuf well-known type bundled with Buf's compiler.
No external module dependency, `buf.lock`, copied Google schema or local `protoc` installation
is needed for these checks.

## Code generation

From the repository root, in PowerShell:

```powershell
Set-Location contracts
buf --version
buf lint
buf format --diff --exit-code
buf generate
```

Use Buf CLI **1.73.0**. Generation uses these remote plugins, with both the upstream version
and the Buf packaging revision pinned:

| Language | Remote plugin | Revision | Output directory |
|:--|:--|:--|:--|
| Java | `buf.build/protocolbuffers/java:v35.1` | 1 | `build/generated/java/` |
| Python | `buf.build/protocolbuffers/python:v35.1` | 1 | `build/generated/python/` |

Run generation from `contracts/`: output paths are relative to the current directory.
Remote generation sends the Protobuf definitions to the Buf service and requires network
access. No local `protoc` or plugin installation is needed. `clean: true` removes only the
two configured output directories before generation, preventing stale generated files.

The Java options keep the Protobuf package `dop.common.v1` unchanged while generating the
Java package `io.github.opendop.contracts.common.v1`. `java_multiple_files = true` makes
`EventMetadata` directly importable. The generated Java files are:

```text
build/generated/java/io/github/opendop/contracts/common/v1/EventMetadata.java
build/generated/java/io/github/opendop/contracts/common/v1/EventMetadataOrBuilder.java
build/generated/java/io/github/opendop/contracts/common/v1/EventMetadataOuterClass.java
```

The generated Python file is:

```text
build/generated/python/dop/common/v1/event_metadata_pb2.py
```

The scoped rule in `contracts/.gitignore` excludes `build/generated/` from Git. No Google
well-known types are generated; the language runtimes supply their implementations. The
matching runtime versions are `com.google.protobuf:protobuf-java:4.35.1` for Java and
`protobuf==7.35.1` for Python. The verification environments below pin these dependencies.
Generation alone does not prove that consumers compile or run.

## Generated-code consumption checks

These are standalone contract checks, not the core or analytics applications. Both use fixed
synthetic data and verify all six fields, Unicode strings, timestamp seconds/nanoseconds and
a binary round-trip within the same language. They are not the cross-language golden test.

### Java

`java/` is a Java 21 library built with Gradle Wrapper **9.8.0** and the built-in `java-library`
plugin. It exposes `protobuf-java:4.35.1` as an API dependency. Strict dependency locking pins
the resolved configurations in `java/gradle.lockfile`; normal verification does not update it.

From the repository root, with the prerequisites above on PATH:

```powershell
Set-Location contracts/java
.\gradlew.bat --no-daemon --console=plain check
```

On Unix, use `./gradlew` (or `sh ./gradlew`). No global Gradle installation is required.
The `generateContracts` task invokes `buf generate` from `contracts/` before Java compilation
when its inputs change or its outputs are missing. It regenerates both language outputs.
Keep Buf 1.73.0 on PATH; remote generation requires network access.

`check` runs `consumptionTest`, a Java program compiled against the generated library. Failed
field checks throw `AssertionError` explicitly, so verification does not depend on `-ea`.
The framework-based Gradle `test` runner is disabled and depends on `consumptionTest`, so
both `check` and `test` execute the standalone verifier. No JUnit or application framework
is needed.

To intentionally refresh dependency locks, run the same command with `--write-locks` and
review the resulting lock file. This is a maintenance operation, not the normal test command.

### Wrapper bootstrap and integrity

The Wrapper is generated with the standard `wrapper` task using the official binary-only
Gradle 9.8.0 distribution in a temporary directory, not a global installation. The ZIP is
verified before executing its Gradle binary. The generated Wrapper JAR is then checked
against D2 and the official [Gradle checksum reference](https://gradle.org/release-checksums/).

| Artifact | SHA-256 |
|:--|:--|
| `gradle-9.8.0-bin.zip` | `bafd5ce9cfaea0fbccfdc8439a1ac42fbd4cd9c89dc9a988228d8a2639a58e6c` |
| `gradle-wrapper.jar` | `238e777fcddd7e34f9708186085def2abd6e08e658505b38718d79d74c21abd5` |

For a bootstrap without existing Gradle, download
`https://downloads.gradle.org/distributions/gradle-9.8.0-bin.zip`, compare its SHA-256 with the
table, and extract it to a temporary directory. With JDK 21 and Buf on PATH, invoke
the extracted `gradle-9.8.0/bin/gradle.bat --no-daemon --console=plain -p contracts/java wrapper`
from the repository root. Verify the generated JAR checksum before using the Wrapper.
`build.gradle.kts` pins the distribution checksum, which is written into
`gradle/wrapper/gradle-wrapper.properties` for future Wrapper downloads.

### Python

`python/` is a uv verification environment, not a publishable package. It requires Python
**3.12.15**, uv **0.12.23** and `protobuf==7.35.1`. `uv.lock` records package artifacts and
hashes. From the repository root, with the prerequisites on PATH and contracts generated:

```powershell
Set-Location contracts/python
uv sync --frozen --python 3.12.15
uv run --frozen --no-sync python -m unittest -v
```

The test adds `contracts/build/generated/python/` to its own import path, leaving system
Python settings untouched. If generation is missing it fails with a command to run first.
It uses standard-library `unittest`; there is no analytics worker or package publishing setup.

To intentionally refresh the Python lock, use
`uv lock --python 3.12.15 --default-index https://pypi.org/simple` and review the diff. Use
`uv sync --frozen` for installations from the committed lock.

`contracts/.gitignore` also excludes the Java build/cache directories, the Python virtual
environment and the test bytecode cache. Wrapper files, lock files and handwritten tests
must remain tracked.

## Bidirectional golden-file interoperability test

From the repository root, with the prerequisites above on PATH:

```powershell
Set-Location contracts/python
uv sync --frozen --python 3.12.15
uv run --frozen --no-sync python run_interop.py
```

The contracts-only CI workflow uses this runner. On Unix the runner invokes
`sh ../java/gradlew`; on Windows it invokes `../java/gradlew.bat`. A nonzero Gradle exit code,
parse error, missing fixture or field mismatch fails the command. For separate directions:

```powershell
uv run --frozen --no-sync python run_interop.py --direction java-to-python
uv run --frozen --no-sync python run_interop.py --direction python-to-java
```

The runner uses the existing pinned Gradle/uv environments and Python standard-library
`subprocess` and `unittest`. Gradle ensures generation and compilation before either
direction, including when generated outputs are initially missing. Network access is needed
when generation or uncached dependencies are required.

- **Java -> Python:** `writeJavaInteropFixture` always serializes a fresh Java message to
  `build/interop/java.bin`. Python reads it and the saved Java golden fixture, checking each
  field against constants in the Python verifier.
- **Python -> Java:** Python always serializes a fresh message to `build/interop/python.bin`.
  `verifyPythonInterop` reads it and the saved Python golden fixture, checking each field
  against constants in the Java verifier.
- Each reader also rejects ten deliberately incorrect messages: four changed strings,
  changed seconds and nanos for both timestamps, and each missing timestamp. These are
  value/presence checks, separate from the completed schema field-renaming experiment below.

`fixtures/java.bin` and `fixtures/python.bin` are small, synthetic Protobuf wire messages
produced by the Java and Python serializers respectively. They are intentional test inputs
to retain in Git, not generated source code. `fixtures/.gitattributes` marks `*.bin` as binary
to prevent Git line-ending conversion from corrupting these small messages, which Git may
otherwise detect as text. Their expected decoded contents are:

| Field | Java fixture | Python fixture |
|:--|:--|:--|
| `event_id` | `event-java-golden-001` | `event-python-golden-001` |
| `tenant_id` | `tenant-örnek` | `tenant-örnek` |
| `project_id` | `project-東京` | `project-東京` |
| `environment` | `test-İstanbul-☃-🚀` | `test-İstanbul-☃-🚀` |
| `occurred_at.seconds` | `1700000000` | `1700000000` |
| `occurred_at.nanos` | `123456789` | `123456789` |
| `observed_at.seconds` | `1700000002` | `1700000002` |
| `observed_at.nanos` | `987654321` | `987654321` |

Both readers explicitly check timestamp presence. The expected strings include accented
Latin characters, Japanese text, a snowman and a supplementary Unicode character (rocket).
Checks compare decoded field values, never cross-language byte equality: Protobuf wire
serialization is not a universal canonical byte representation.

Normal runs never rewrite the saved golden fixtures. To deliberately recreate them with the
pinned producers, first run the complete test successfully, then, from `contracts/python/`:

```powershell
Copy-Item ../build/interop/java.bin ../fixtures/java.bin
Copy-Item ../build/interop/python.bin ../fixtures/python.bin
uv run --frozen --no-sync python run_interop.py
```

Review any fixture change together with the expected values above and both independent
reader implementations. A byte difference alone does not imply incompatibility. Fresh
outputs under `build/interop/` are ignored by Git. The runner does not add interoperability
to Java-only `check` or Python-only unittest discovery: run all three verification commands
when checking contracts, so a Java-only task does not implicitly require uv.

## Contracts-only CI

`.github/workflows/contracts-ci.yml` runs one `ubuntu-24.04` job for pull requests targeting
`main` when `contracts/**` or the workflow itself changes. Both maintainers agreed to bring
forward this minimum WP2 verification; general CI and release automation remain in WP6.

The job sets up the pinned Temurin 21, Buf and uv versions, uses the checked-in Gradle
Wrapper 9.8.0 and uv-managed Python 3.12.15, and installs Python dependencies with the frozen
lock. It runs Buf lint, non-mutating format checks, remote generation, Java compilation and
consumption, Python unittest, then each golden-test direction as a separate step. It invokes
the existing tests without duplicating their implementations.

Actions are pinned to verified full commit SHAs. The token has only `contents: read`;
checkout credentials are not persisted, PR comments and cache uploads are disabled, and no
repository secrets, publishing or write permissions are needed. Remote generation and
first-time tool/dependency downloads require network access.

The workflow is implemented, but CI success and Linux compatibility are not yet proven by
a real GitHub Actions run. After the branch is pushed and a PR targets `main`, inspect the
`Contracts CI` run, including both golden steps, and retain its URL and tested commit SHA
as evidence. Local success alone does not satisfy the CI acceptance criterion.

## Completed field-renaming experiment

On 19 October 2026, an isolated temporary schema copy changed only:

```diff
- string event_id = 1;
+ string event_reference = 1;
```

Field number `1` and type `string` stayed unchanged. The same pinned remote generators
produced Java and Python classes in temporary directories. Byte-for-byte unchanged copies
of the original consumption tests were used; the authoritative schema, existing generated
files and golden fixtures were never overwritten.

The experimental Java classes compiled independently with `protobuf-java:4.35.1`. Compiling
the unchanged Java consumer against them failed because the old accessors no longer existed.
Relevant excerpts from the actual compiler output:

```text
  symbol:   method setEventId(String)
  location: class Builder
  symbol:   method getEventId()
  location: variable message of type EventMetadata
2 errors
Unchanged Java consumer javac exit code: 1
```

The experimental Python module imported successfully with Python 3.12.15 / protobuf 7.35.1
and exposed `event_reference` as string field `1`, with no `event_id` field. Both unchanged
consumption tests failed while constructing the message. Actual failure evidence:

```text
ValueError: Protocol message EventMetadata has no "event_id" field.
Ran 2 tests in 0.001s
FAILED (errors=2)
Unchanged Python consumer exit code: 1
```

These failures demonstrate source-level incompatibility: existing consumer code uses field
names and generated accessors. They do not prove binary wire-format incompatibility; a
field-name-only change with the same number and type need not break binary parsing.

The temporary experimental outputs were removed. The original schema still contained
`string event_id = 1;`, and the original Java/Python consumption checks, both golden-test
directions, Buf lint/format and Git whitespace checks passed afterward. Schema and golden
fixture hashes matched their previously recorded values. No restoration of the authoritative
files was needed because they had not been changed.

## Troubleshooting

- **Incorrect `JAVA_HOME`:** point it to the JDK 21 root, then check `java -version` and
  `javac -version` in the same terminal. Restart an existing terminal if environment changes
  are not visible. The Wrapper needs a suitable JDK before it can start.
- **Missing generated classes:** run `buf generate` from `contracts/` before the standalone
  Python tests. Java compilation and the interoperability runner trigger generation when
  required. Do not copy generated code into source directories or install local `protoc`.
- **Remote generation failure:** check network/proxy access to `buf.build` and inspect the
  Buf error. Keep the pinned plugin versions and revisions; do not substitute a different
  generator or treat missing/stale outputs as a successful generation.

## Remaining acceptance criteria

WP2's contract foundation and local consumption/interoperability verification are
implemented, including the field-renaming experiment. Full acceptance remains open:

| Work package | Remaining responsibility |
|:--|:--|
| WP3 | Integrate generated Java contracts with the real core build |
| WP4 | Integrate generated Python contracts with the real analytics build |
| WP2 exception | Obtain real GitHub Actions evidence from the contracts-only workflow, including Linux and both golden-test directions |
| WP6 | Implement broader CI and release automation; contracts-only workflow is already defined |

These standalone checks do not establish those integrations or fully close WP2. The required
breaking-change gate starts at the v0.1.0 tag, using that release as its baseline.
