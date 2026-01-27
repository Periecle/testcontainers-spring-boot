package com.playtika.testcontainer.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
class GoogleCloudStorageHttpClient {

    private final HttpClient httpClient;

    GoogleCloudStorageHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public void sendUpdateConfigRequest(String containerEndpoint) throws IOException {
        try {
            String requestBody = "{"
                    + "\"externalUrl\": \"" + containerEndpoint + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(containerEndpoint + "/_internal/config"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int responseCode = response.statusCode();

            if (responseCode != 200) {
                log.error(
                        "error updating Google Cloud Fake Storage Server with external url, response status code {} != 200 message {}",
                        responseCode,
                        response.body());
            }
        } catch (Exception e) {
            log.error("error updating Google Cloud Fake Storage Server with external host", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IOException(e);
        }
    }
}
