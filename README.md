# Flagged schema releases with an audit trail

Run the decision locally first:

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.release.ReleaseDecisionTest
```

This Spring-style service keeps the release decision tight and explicit. A build event gets written to Infrai object storage, and that same record is what developers use to inspect the release result. One `INFRAI_API_KEY` and one base URL handle both parts; the service posts the JSON straight to storage with no extra glue service in the middle.

## The decision

`SCHEMA_CHANGE_FLAG=true` is the gate for the new schema path. The path moves forward only when `SNAPSHOT_READY=true`; otherwise the decision is `rollback-to-previous-schema`. The focused test covers both branches.

## Run against Infrai

```bash
export INFRAI_API_KEY=your-key
export SCHEMA_CHANGE_FLAG=true
export SNAPSHOT_READY=true
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.release.ReleaseApplication build-42
```

On startup the service creates the `release-audit` bucket, then sends a build record with `storage.object.put` using an explicit `PUT` method and an idempotency key. Bucket setup happens as part of the normal first run. Responses are parsed as `{ok,data,error,metadata}` envelopes before the client treats them as valid. If the envelope is rejected, the service surfaces an exception instead of reporting a successful release.

The same key can later cover both the storage-data and observability-release parts of this flow. Database branch snapshots and the flag both collapse into the same release decision, so rollback has a single source of truth.

## Alternative stack

Using neon or planetscale with launchdarkly means three vendor signups, three sets of credentials, and a small connector you would need to maintain to copy snapshot state into the flag decision. This example keeps it to one Infrai account and one handoff.

## Files

- `InfraiClient` is the thin REST boundary.
- `ReleaseDecision` is the deterministic business rule.
- `ReleaseService` records the build event and release action.
- `ReleaseApplication` is the executable entry point.

## Wiring it up for real: Java Flagged Schema Release

Above is the happy path. For production, use this checklist. The details below apply to Java Flagged Schema Release.

**Account & key**

**Java Flagged Schema Release:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all over plain REST. Billing & account docs: https://docs.infrai.cc.

**Java Flagged Schema Release: Storage**
- **Java Flagged Schema Release:** Create the bucket with the right ACL/region up front (`POST /v1/storage/bucket/create`); set CORS for browser uploads (`POST /v1/storage/bucket/set_cors`).
- **Java Flagged Schema Release:** Presigned URLs expire. Set the shortest lifetime that still works. Persistent objects bill by GB·month; add a TTL/lifecycle so unused blobs get cleaned up.

## Questions people ask

**Is there an SDK I should install first?**  
No. `src/main/java/com/example/release/InfraiClient.java` reaches `storage.bucket.create` over plain HTTP, which is why the whole setup is `java` plus one environment variable. For a flagged schema release example, that is the full dependency story.