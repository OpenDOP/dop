package io.github.opendop.contracts;

import com.google.protobuf.Timestamp;
import io.github.opendop.contracts.common.v1.EventMetadata;
import java.util.Objects;

/** Standalone verification of the generated contract, without a test framework. */
public final class EventMetadataConsumption {
    private EventMetadataConsumption() {}

    public static void main(String[] args) throws Exception {
        EventMetadata original = EventMetadata.newBuilder()
                .setEventId("event-java-001")
                .setTenantId("tenant-test")
                .setProjectId("project-test")
                .setEnvironment("test-\u2603")
                .setOccurredAt(Timestamp.newBuilder().setSeconds(1_700_000_000L).setNanos(123_456_789))
                .setObservedAt(Timestamp.newBuilder().setSeconds(1_700_000_002L).setNanos(987_654_321))
                .build();

        verifyFields(original);
        System.out.println("PASS: generated EventMetadata imported and all six fields verified");

        byte[] encoded = original.toByteArray();
        if (encoded.length == 0) {
            throw new AssertionError("Populated EventMetadata serialized to empty bytes");
        }
        EventMetadata parsed = EventMetadata.parseFrom(encoded);
        verifyFields(parsed);
        requireEqual(original, parsed, "round-trip message");
        System.out.println("PASS: Java binary round-trip preserves strings and timestamp nanoseconds");
    }

    private static void verifyFields(EventMetadata message) {
        requireEqual("event-java-001", message.getEventId(), "event_id");
        requireEqual("tenant-test", message.getTenantId(), "tenant_id");
        requireEqual("project-test", message.getProjectId(), "project_id");
        requireEqual("test-\u2603", message.getEnvironment(), "environment");
        requireEqual(true, message.hasOccurredAt(), "occurred_at presence");
        requireEqual(1_700_000_000L, message.getOccurredAt().getSeconds(), "occurred_at seconds");
        requireEqual(123_456_789, message.getOccurredAt().getNanos(), "occurred_at nanos");
        requireEqual(true, message.hasObservedAt(), "observed_at presence");
        requireEqual(1_700_000_002L, message.getObservedAt().getSeconds(), "observed_at seconds");
        requireEqual(987_654_321, message.getObservedAt().getNanos(), "observed_at nanos");
    }

    private static void requireEqual(Object expected, Object actual, String field) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(field + ": expected " + expected + ", got " + actual);
        }
    }
}
