package com.example.release;

public final class ReleaseApplication {
    public static void main(String[] args) throws Exception {
        String buildId = args.length > 0 ? args[0] : "build-local";
        boolean flag = Boolean.parseBoolean(System.getenv().getOrDefault("SCHEMA_CHANGE_FLAG", "false"));
        boolean snapshot = Boolean.parseBoolean(System.getenv().getOrDefault("SNAPSHOT_READY", "true"));
        ReleaseService service = new ReleaseService(new InfraiClient(System.getenv("INFRAI_API_KEY")));
        ReleaseDecision decision = service.release(buildId, flag, snapshot);
        System.out.println(decision.action());
    }
}
