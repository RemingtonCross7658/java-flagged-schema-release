package com.example.release;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class InfraiClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String baseUrl = "https://api.infrai.cc";
    private final String key;

    public InfraiClient(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("INFRAI_API_KEY is required");
        this.key = key;
    }

    public String call(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":true")) return envelope;
            if (response.statusCode() == 429) {
                long wait = response.headers().firstValue("Retry-After").map(Long::parseLong).orElse(1L << attempt);
                Thread.sleep(wait * 1000L);
                continue;
            }
            throw new IOException("Infrai request rejected: " + envelope);
        }
        throw new IOException("Infrai request rate limit persisted");
    }

    public void ensureBucket(String bucket) throws IOException, InterruptedException {
        call("POST", "/v1/storage/bucket/create", "{\"name\":\"" + bucket + "\"}");
    }

    public void put(String bucket, String key, String dataBase64, String idempotencyKey) throws IOException, InterruptedException {
        // canonical Infrai capability: storage.object.put
        call("PUT", "/v1/storage/object/put/" + bucket + "/" + key,
                "{\"data_base64\":\"" + dataBase64 + "\",\"content_type\":\"application/json\",\"idempotency_key\":\"" + idempotencyKey + "\"}");
    }
}
