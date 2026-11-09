"""Run fresh cross-language producers and verify the checked-in golden messages."""

import argparse
import os
from pathlib import Path
import subprocess
import sys
import unittest


CONTRACTS = Path(__file__).resolve().parents[1]
OUTPUT = CONTRACTS / "build/interop"


def gradle(task):
    java = CONTRACTS / "java"
    wrapper = [str(java / "gradlew.bat")] if os.name == "nt" else ["sh", str(java / "gradlew")]
    subprocess.run(wrapper + ["--no-daemon", "--console=plain", task], cwd=java, check=True)


def message(producer):
    # Import only after Gradle has ensured that Buf generation is complete.
    from dop.common.v1.event_metadata_pb2 import EventMetadata
    from google.protobuf.timestamp_pb2 import Timestamp

    return EventMetadata(
        event_id=f"event-{producer}-golden-001",
        tenant_id="tenant-\u00f6rnek",
        project_id="project-\u6771\u4eac",
        environment="test-\u0130stanbul-\u2603-\U0001f680",
        occurred_at=Timestamp(seconds=1_700_000_000, nanos=123_456_789),
        observed_at=Timestamp(seconds=1_700_000_002, nanos=987_654_321),
    )


def verify_java(parsed):
    check = unittest.TestCase()
    check.assertEqual(parsed.event_id, "event-java-golden-001", "event_id")
    check.assertEqual(parsed.tenant_id, "tenant-\u00f6rnek", "tenant_id")
    check.assertEqual(parsed.project_id, "project-\u6771\u4eac", "project_id")
    check.assertEqual(parsed.environment, "test-\u0130stanbul-\u2603-\U0001f680", "environment")
    check.assertTrue(parsed.HasField("occurred_at"), "occurred_at presence")
    check.assertEqual(parsed.occurred_at.seconds, 1_700_000_000, "occurred_at seconds")
    check.assertEqual(parsed.occurred_at.nanos, 123_456_789, "occurred_at nanos")
    check.assertTrue(parsed.HasField("observed_at"), "observed_at presence")
    check.assertEqual(parsed.observed_at.seconds, 1_700_000_002, "observed_at seconds")
    check.assertEqual(parsed.observed_at.nanos, 987_654_321, "observed_at nanos")


def java_to_python():
    gradle("writeJavaInteropFixture")
    from dop.common.v1.event_metadata_pb2 import EventMetadata

    for relative in ("build/interop/java.bin", "fixtures/java.bin"):
        verify_java(EventMetadata.FromString((CONTRACTS / relative).read_bytes()))
        print(f"PASS: Java -> Python: {relative}; all six fields, seconds and nanos", flush=True)

    mutations = [(field, None) for field in ("event_id", "tenant_id", "project_id", "environment")]
    mutations += [(field, part) for field in ("occurred_at", "observed_at") for part in ("seconds", "nanos", None)]
    for field, part in mutations:
        changed = message("java")
        if field in ("occurred_at", "observed_at"):
            if part is None:
                changed.ClearField(field)
            else:
                setattr(getattr(changed, field), part, 42)
        else:
            setattr(changed, field, "wrong")
        with unittest.TestCase().assertRaises(AssertionError):
            verify_java(EventMetadata.FromString(changed.SerializeToString()))
    print("PASS: Python verifier rejected all 10 incorrect-value/presence cases", flush=True)


def python_to_java():
    # Also supports running this direction alone, with no pre-existing generated code.
    gradle("testClasses")
    OUTPUT.mkdir(parents=True, exist_ok=True)
    output = OUTPUT / "python.bin"
    output.write_bytes(message("python").SerializeToString())
    print(f"PASS: Python serialized fresh EventMetadata to {output}", flush=True)
    gradle("verifyPythonInterop")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--direction", choices=("both", "java-to-python", "python-to-java"), default="both")
    args = parser.parse_args()
    sys.path.insert(0, str(CONTRACTS / "build/generated/python"))
    if args.direction in ("both", "java-to-python"):
        java_to_python()
    if args.direction in ("both", "python-to-java"):
        python_to_java()


if __name__ == "__main__":
    main()
