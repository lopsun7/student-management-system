package com.studentmanagement.client;

import com.studentmanagement.config.DownstreamProperties;
import com.studentmanagement.dto.DownstreamNameRequest;
import com.studentmanagement.dto.DownstreamNameResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DownstreamAggregationClient {

	private final RestClient restClient;
	private final DownstreamProperties downstreamProperties;

	public DownstreamAggregationClient(
			RestClient.Builder restClientBuilder,
			DownstreamProperties downstreamProperties) {
		this.restClient = restClientBuilder.baseUrl(downstreamProperties.getBaseUrl()).build();
		this.downstreamProperties = downstreamProperties;
	}

	public DownstreamNameResponse aggregate(String aggregatedName) {
		return restClient.post()
			.uri(downstreamProperties.getAggregationPath())
			.contentType(MediaType.APPLICATION_JSON)
			.body(new DownstreamNameRequest(aggregatedName))
			.retrieve()
			.body(DownstreamNameResponse.class);
	}

}
