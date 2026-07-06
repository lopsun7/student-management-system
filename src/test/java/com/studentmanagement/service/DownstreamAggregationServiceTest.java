package com.studentmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.studentmanagement.dto.UpstreamNameResponse;
import com.studentmanagement.repository.FailedDownstreamRequestRepository;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(properties = {
	"downstream.aggregation-path=/name/aggregation",
	"downstream.default-name=Steven",
	"resilience4j.retry.instances.downstreamAggregation.max-attempts=3",
	"resilience4j.retry.instances.downstreamAggregation.wait-duration=1ms",
	"resilience4j.circuitbreaker.instances.downstreamAggregation.minimum-number-of-calls=100"
})
class DownstreamAggregationServiceTest {

	private static final TestDownstreamServer DOWNSTREAM_SERVER = new TestDownstreamServer();

	@Autowired
	private DownstreamAggregationService downstreamAggregationService;

	@MockBean
	private FailedDownstreamRequestRepository failedDownstreamRequestRepository;

	@MockBean
	private DownstreamRecoveryService downstreamRecoveryService;

	@DynamicPropertySource
	static void downstreamProperties(DynamicPropertyRegistry registry) {
		DOWNSTREAM_SERVER.start();
		registry.add("downstream.base-url", DOWNSTREAM_SERVER::baseUrl);
	}

	@BeforeEach
	void setUp() {
		DOWNSTREAM_SERVER.reset();
		reset(failedDownstreamRequestRepository, downstreamRecoveryService);
	}

	@AfterAll
	static void tearDown() {
		DOWNSTREAM_SERVER.stop();
	}

	@Test
	void shouldForwardPrefixedNamesToDownstream() {
		DOWNSTREAM_SERVER.enqueue(200, """
			{"name":"Steven, Jessica, Amy, Celine"}
			""");

		UpstreamNameResponse response = downstreamAggregationService.aggregateNames(List.of("Jessica", "Amy"));

		assertThat(response.name()).containsExactly("Steven", "Jessica", "Amy", "Celine");
		assertThat(DOWNSTREAM_SERVER.requestBodies()).containsExactly("{\"name\":\"Steven, Jessica, Amy\"}");
	}

	@Test
	void shouldForwardDefaultNameWhenInputIsBlank() {
		DOWNSTREAM_SERVER.enqueue(200, """
			{"name":"Steven, Celine"}
			""");

		UpstreamNameResponse response = downstreamAggregationService.aggregateNames(List.of("   ", ""));

		assertThat(response.name()).containsExactly("Steven", "Celine");
		assertThat(DOWNSTREAM_SERVER.requestBodies()).containsExactly("{\"name\":\"Steven\"}");
	}

	@Test
	void shouldReturnFallbackAndPersistRequestWhenDownstreamFails() {
		when(failedDownstreamRequestRepository.savePending(eq("Steven, Jessica"), anyString()))
			.thenReturn(99L);
		DOWNSTREAM_SERVER.enqueue(500, "{}");
		DOWNSTREAM_SERVER.enqueue(500, "{}");
		DOWNSTREAM_SERVER.enqueue(500, "{}");

		UpstreamNameResponse response = downstreamAggregationService.aggregateNames(List.of("Jessica"));

		assertThat(response.name()).containsExactly("Steven", "Jessica");
		assertThat(DOWNSTREAM_SERVER.requestBodies()).containsExactly(
			"{\"name\":\"Steven, Jessica\"}",
			"{\"name\":\"Steven, Jessica\"}",
			"{\"name\":\"Steven, Jessica\"}"
		);
		verify(failedDownstreamRequestRepository).savePending(eq("Steven, Jessica"), anyString());
		verify(downstreamRecoveryService).retryFailedRequestAsync(99L);
	}

	private record MockHttpResponse(int status, String body) {
	}

	private static class TestDownstreamServer {

		private final Queue<MockHttpResponse> responses = new ConcurrentLinkedQueue<>();
		private final List<String> requestBodies = new CopyOnWriteArrayList<>();
		private HttpServer server;

		void start() {
			if (server != null) {
				return;
			}
			try {
				server = HttpServer.create(new InetSocketAddress(0), 0);
				server.createContext("/name/aggregation", exchange -> {
					requestBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
					MockHttpResponse response = responses.poll();
					if (response == null) {
						response = new MockHttpResponse(500, "{}");
					}
					byte[] body = response.body().getBytes(StandardCharsets.UTF_8);
					exchange.getResponseHeaders().set("Content-Type", "application/json");
					exchange.sendResponseHeaders(response.status(), body.length);
					exchange.getResponseBody().write(body);
					exchange.close();
				});
				server.start();
			} catch (IOException exception) {
				throw new IllegalStateException("Failed to start test downstream server", exception);
			}
		}

		void enqueue(int status, String body) {
			responses.add(new MockHttpResponse(status, body));
		}

		void reset() {
			responses.clear();
			requestBodies.clear();
		}

		String baseUrl() {
			return "http://localhost:" + server.getAddress().getPort();
		}

		List<String> requestBodies() {
			return requestBodies;
		}

		void stop() {
			if (server != null) {
				server.stop(0);
			}
		}

	}

}
