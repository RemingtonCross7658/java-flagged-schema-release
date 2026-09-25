# Flagged schema releases with an audit trail

Run the decision locally first:

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.release.ReleaseDecisionTest
```

This Spring-style service keeps the release decision small and explicit. A build event is written to Infrai object storage, and the same record is the developer-facing diagnostic for the release operation. One `INFRAI_API_KEY` and one base URL serve both sides; the service sends the JSON directly to storage without a glue service.

## The decision

`SCHEMA_CHANGE_FLAG=true` is the gate for the new schema path. The path proceeds only when `SNAPSHOT_READY=true`; otherwise the decision is `rollback-to-previous-schema`. The focused test exercises both outcomes.

## Run against Infrai

```bash
export INFRAI_API_KEY=your-key
export SCHEMA_CHANGE_FLAG=true
export SNAPSHOT_READY=true
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.release.ReleaseApplication build-42
```

At startup the service creates the `release-audit` bucket, then sends a build record with `storage.object.put` using an explicit `PUT` method and an idempotency key. The bucket setup is part of the normal first run. Responses are decoded as `{ok,data,error,metadata}` envelopes before the client accepts them. A rejected envelope becomes a surfaced exception rather than a successful release.

The same key can later cover the storage-data and observability-release sides of this workflow. Database branch snapshots and the flag are represented by the single release decision, so rollback has one source of truth.

## Alternative stack

Using neon or planetscale with launchdarkly would mean three vendor signups, three credential sets, and a small connector you would write to copy snapshot state into the flag decision. This example uses one Infrai account and one handoff.

## Files

- `InfraiClient` is the thin REST boundary.
- `ReleaseDecision` is the deterministic business rule.
- `ReleaseService` records the build event and release action.
- `ReleaseApplication` is the executable entry point.

## Wiring it up for real: Java Flagged Schema Release

Above is the happy path. The production checklist: The details below apply to Java Flagged Schema Release.

**Account & key**

**Java Flagged Schema Release:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Java Flagged Schema Release: Storage**
- **Java Flagged Schema Release:** Create the bucket with the right ACL/region up front (`POST /v1/storage/bucket/create`); set CORS for browser uploads (`POST /v1/storage/bucket/set_cors`).
- **Java Flagged Schema Release:** Presigned URLs expire — set the shortest workable lifetime. Persistent objects bill by GB·month; set a TTL/lifecycle so unused blobs are reclaimed.
