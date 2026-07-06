package com.studentmanagement.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.studentmanagement.config.DownstreamProperties;
import com.studentmanagement.dto.DownstreamNameResponse;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class DownstreamAggregationClientTest {

	private final AtomicReference<String> requestBody = new AtomicReference<>();
	private HttpServer server;
	private DownstreamAggregationClient client;

	@BeforeEach
	void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/name/aggregation", exchange -> {
			requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			byte[] response = "{\"name\":\"Steven, Jessica, Celine\"}".getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});
		server.start();

		DownstreamProperties properties = new DownstreamProperties();
		properties.setBaseUrl("http://localhost:" + server.getAddress().getPort());
		properties.setAggregationPath("/name/aggregation");
		client = new DownstreamAggregationClient(RestClient.builder(), properties);
	}

	@AfterEach
	void tearDown() {
		server.stop(0);
	}

	@Test
	void shouldPostAggregatedNameToConfiguredDownstreamEndpoint() {
		DownstreamNameResponse response = client.aggregate("Steven, Jessica");

		assertThat(response.name()).isEqualTo("Steven, Jessica, Celine");
		assertThat(requestBody.get()).isEqualTo("{\"name\":\"Steven, Jessica\"}");
	}
}
