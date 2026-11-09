"""Verify the generated Python contract without an analytics application."""

from pathlib import Path
import sys
import unittest


GENERATED_ROOT = Path(__file__).resolve().parents[1] / "build/generated/python"
if not (GENERATED_ROOT / "dop/common/v1/event_metadata_pb2.py").is_file():
    raise RuntimeError("Generated contracts are missing; run 'buf generate' in contracts/ first")
sys.path.insert(0, str(GENERATED_ROOT))

from dop.common.v1.event_metadata_pb2 import EventMetadata
from google.protobuf.timestamp_pb2 import Timestamp


class EventMetadataConsumptionTest(unittest.TestCase):
    def setUp(self):
        self.message = EventMetadata(
            event_id="event-python-001",
            tenant_id="tenant-test",
            project_id="project-test",
            environment="test-\u2603",
            occurred_at=Timestamp(seconds=1_700_000_000, nanos=123_456_789),
            observed_at=Timestamp(seconds=1_700_000_002, nanos=987_654_321),
        )

    def verify_fields(self, message):
        self.assertEqual(message.event_id, "event-python-001")
        self.assertEqual(message.tenant_id, "tenant-test")
        self.assertEqual(message.project_id, "project-test")
        self.assertEqual(message.environment, "test-\u2603")
        self.assertTrue(message.HasField("occurred_at"))
        self.assertEqual(message.occurred_at.seconds, 1_700_000_000)
        self.assertEqual(message.occurred_at.nanos, 123_456_789)
        self.assertTrue(message.HasField("observed_at"))
        self.assertEqual(message.observed_at.seconds, 1_700_000_002)
        self.assertEqual(message.observed_at.nanos, 987_654_321)

    def test_all_six_fields(self):
        self.verify_fields(self.message)

    def test_binary_round_trip(self):
        encoded = self.message.SerializeToString()
        self.assertGreater(len(encoded), 0)
        parsed = EventMetadata.FromString(encoded)
        self.verify_fields(parsed)
        self.assertEqual(parsed, self.message)


if __name__ == "__main__":
    unittest.main()
