package io.github.opendop.contracts;

import com.google.protobuf.Timestamp;
import io.github.opendop.contracts.common.v1.EventMetadata;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Java producer and Python-message verifier for the cross-language fixture test. */
public final class EventMetadataInterop {
    private EventMetadataInterop() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Expected write|verify and the contracts directory");
        }
        Path contracts = Path.of(args[1]);
        switch (args[0]) {
            case "write" -> {
                Path output = contracts.resolve("build/interop/java.bin");
                Files.createDirectories(output.getParent());
                Files.write(output, expected("java").toByteArray());
                System.out.println("PASS: Java serialized fresh EventMetadata to " + output);
            }
            case "verify" -> {
                for (String file : List.of("build/interop/python.bin", "fixtures/python.bin")) {
                    EventMetadata parsed = EventMetadata.parseFrom(Files.readAllBytes(contracts.resolve(file)));
                    verifyPython(parsed);
                    System.out.println("PASS: Python -> Java: " + file + "; all six fields, seconds and nanos");
                }
                verifyRejections();
            }
            default -> throw new IllegalArgumentException("Unknown mode: " + args[0]);
        }
    }

    private static EventMetadata expected(String producer) {
        return EventMetadata.newBuilder()
                .setEventId("event-" + producer + "-golden-001")
                .setTenantId("tenant-\u00f6rnek")
                .setProjectId("project-\u6771\u4eac")
                .setEnvironment("test-\u0130stanbul-\u2603-\ud83d\ude80")
                .setOccurredAt(Timestamp.newBuilder().setSeconds(1_700_000_000L).setNanos(123_456_789))
                .setObservedAt(Timestamp.newBuilder().setSeconds(1_700_000_002L).setNanos(987_654_321))
                .build();
    }

    private static void verifyPython(EventMetadata message) {
        EventMetadata expected = expected("python");
        requireEqual(expected.getEventId(), message.getEventId(), "event_id");
        requireEqual(expected.getTenantId(), message.getTenantId(), "tenant_id");
        requireEqual(expected.getProjectId(), message.getProjectId(), "project_id");
        requireEqual(expected.getEnvironment(), message.getEnvironment(), "environment");
        requireEqual(true, message.hasOccurredAt(), "occurred_at presence");
        requireEqual(expected.getOccurredAt().getSeconds(), message.getOccurredAt().getSeconds(), "occurred_at seconds");
        requireEqual(expected.getOccurredAt().getNanos(), message.getOccurredAt().getNanos(), "occurred_at nanos");
        requireEqual(true, message.hasObservedAt(), "observed_at presence");
        requireEqual(expected.getObservedAt().getSeconds(), message.getObservedAt().getSeconds(), "observed_at seconds");
        requireEqual(expected.getObservedAt().getNanos(), message.getObservedAt().getNanos(), "observed_at nanos");
    }

    private static void verifyRejections() throws Exception {
        List<Consumer<EventMetadata.Builder>> mutations = List.of(
                b -> b.setEventId("wrong"),
                b -> b.setTenantId("wrong"),
                b -> b.setProjectId("wrong"),
                b -> b.setEnvironment("wrong"),
                b -> b.getOccurredAtBuilder().setSeconds(42),
                b -> b.getOccurredAtBuilder().setNanos(42),
                b -> b.getObservedAtBuilder().setSeconds(42),
                b -> b.getObservedAtBuilder().setNanos(42),
                EventMetadata.Builder::clearOccurredAt,
                EventMetadata.Builder::clearObservedAt);
        for (Consumer<EventMetadata.Builder> mutation : mutations) {
            EventMetadata.Builder changed = expected("python").toBuilder();
            mutation.accept(changed);
            EventMetadata parsed = EventMetadata.parseFrom(changed.build().toByteArray());
            boolean rejected = false;
            try {
                verifyPython(parsed);
            } catch (AssertionError expectedFailure) {
                rejected = true;
            }
            if (!rejected) {
                throw new AssertionError("Java verifier accepted an incorrect field value or absent timestamp");
            }
        }
        System.out.println("PASS: Java verifier rejected all 10 incorrect-value/presence cases");
    }

    private static void requireEqual(Object expected, Object actual, String field) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(field + ": expected " + expected + ", got " + actual);
        }
    }
}
