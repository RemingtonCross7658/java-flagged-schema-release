package com.example.release;

public record ReleaseDecision(boolean proceed, String action) {
    public static ReleaseDecision evaluate(boolean schemaFlag, boolean snapshotReady) {
        if (schemaFlag && snapshotReady) return new ReleaseDecision(true, "apply-schema-and-release");
        return new ReleaseDecision(false, "rollback-to-previous-schema");
    }
}
