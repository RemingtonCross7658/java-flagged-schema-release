package com.example.release;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class ReleaseService {
    private final InfraiClient infrai;
    private final String bucket = "release-audit";

    public ReleaseService(InfraiClient infrai) { this.infrai = infrai; }

    public ReleaseDecision release(String buildId, boolean schemaFlag, boolean snapshotReady) throws Exception {
        ReleaseDecision decision = ReleaseDecision.evaluate(schemaFlag, snapshotReady);
        infrai.ensureBucket(bucket);
        String event = "{\"build_id\":\"" + buildId + "\",\"action\":\"" + decision.action() + "\"}";
        String encoded = Base64.getEncoder().encodeToString(event.getBytes(StandardCharsets.UTF_8));
        infrai.put(bucket, "builds/" + buildId + ".json", encoded, UUID.randomUUID().toString());
        return decision;
    }
}
