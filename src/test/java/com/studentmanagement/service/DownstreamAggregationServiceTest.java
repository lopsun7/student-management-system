package com.studentmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.studentmanagement.dto.DownstreamNameResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class DownstreamAggregationServiceTest {

	private MockRestServiceServer mockServer;
	private DownstreamAggregationService downstreamAggregationService;

	@BeforeEach
	void setUp() {
		RestClient.Builder restClientBuilder = RestClient.builder();
		mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
		downstreamAggregationService = new DownstreamAggregationService(
			restClientBuilder,
			"http://downstream.test:8080",
			"/name/aggregation",
			"Steven"
		);
	}

	@Test
	void shouldSendProvidedNameToDownstream() {
		mockServer.expect(requestTo("http://downstream.test:8080/name/aggregation"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("""
				{"name":"Steven"}
				"""))
			.andRespond(withSuccess("""
				{"name":"Steven, Jocelyn"}
				""", MediaType.APPLICATION_JSON));

		DownstreamNameResponse response = downstreamAggregationService.aggregateName("Steven");

		assertThat(response.name()).isEqualTo("Steven, Jocelyn");
		mockServer.verify();
	}

	@Test
	void shouldUseDefaultNameWhenInputIsBlank() {
		mockServer.expect(requestTo("http://downstream.test:8080/name/aggregation"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("""
				{"name":"Steven"}
				"""))
			.andRespond(withSuccess("""
				{"name":"Steven, Jocelyn"}
				""", MediaType.APPLICATION_JSON));

		DownstreamNameResponse response = downstreamAggregationService.aggregateName("   ");

		assertThat(response.name()).isEqualTo("Steven, Jocelyn");
		mockServer.verify();
	}

}
