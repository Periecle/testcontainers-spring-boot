package com.playtika.testcontainer.storage;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class GoogleCloudStorageHttpClientTest {

    private HttpServer httpServer;
    private int port;
    private GoogleCloudStorageHttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.start();
        port = httpServer.getAddress().getPort();
        client = new GoogleCloudStorageHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void shouldSendUpdateConfigRequest() throws IOException {
        AtomicReference<String> receivedBody = new AtomicReference<>();
        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedContentType = new AtomicReference<>();

        httpServer.createContext("/_internal/config", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            try (InputStream is = exchange.getRequestBody()) {
                receivedBody.set(new String(is.readAllBytes(), StandardCharsets.UTF_8));
            }
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        String containerEndpoint = "http://localhost:" + port;
        client.sendUpdateConfigRequest(containerEndpoint);

        assertThat(receivedMethod.get()).isEqualTo("PUT");
        assertThat(receivedContentType.get()).isEqualTo("application/json");
        assertThat(receivedBody.get()).isEqualTo("{\"externalUrl\": \"" + containerEndpoint + "\"}");
    }

    @Test
    void shouldHandleErrorResponse() {
        httpServer.createContext("/_internal/config", exchange -> {
            String response = "Error";
            exchange.sendResponseHeaders(500, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        String containerEndpoint = "http://localhost:" + port;

        assertThatCode(() -> client.sendUpdateConfigRequest(containerEndpoint))
                .doesNotThrowAnyException();
    }
}
