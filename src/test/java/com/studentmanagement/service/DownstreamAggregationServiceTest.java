package com.studentmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.studentmanagement.dto.DownstreamNameResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class DownstreamAggregationServiceTest {

	private final RestClient.Builder restClientBuilder = RestClient.builder();
	private final MockRestServiceServer mockRestServiceServer = MockRestServiceServer.bindTo(restClientBuilder).build();
	private final DownstreamAggregationService downstreamAggregationService =
		new DownstreamAggregationService(restClientBuilder, "http://downstream.test", "/name/aggregation", "Steven");

	@Test
	void shouldForwardPrefixedNamesToDownstream() {
		mockRestServiceServer.expect(requestTo("http://downstream.test/name/aggregation"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(content().json("""
				{"name":"Steven, Jessica, Amy"}
				"""))
			.andRespond(withSuccess("""
				{"name":"Steven, Jessica, Amy, Celine"}
				""", MediaType.APPLICATION_JSON));

		DownstreamNameResponse response = downstreamAggregationService.aggregateNames(java.util.List.of("Jessica", "Amy"));

		assertThat(response.name()).isEqualTo("Steven, Jessica, Amy, Celine");
		mockRestServiceServer.verify();
	}

	@Test
	void shouldForwardDefaultNameWhenInputIsBlank() {
		mockRestServiceServer.expect(requestTo("http://downstream.test/name/aggregation"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(content().json("""
				{"name":"Steven"}
				"""))
			.andRespond(withSuccess("""
				{"name":"Steven, Celine"}
				""", MediaType.APPLICATION_JSON));

		DownstreamNameResponse response = downstreamAggregationService.aggregateNames(java.util.List.of("   ", ""));

		assertThat(response.name()).isEqualTo("Steven, Celine");
		mockRestServiceServer.verify();
	}

}
