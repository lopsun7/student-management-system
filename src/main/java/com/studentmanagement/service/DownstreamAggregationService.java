package com.studentmanagement.service;

import com.studentmanagement.dto.DownstreamNameRequest;
import com.studentmanagement.dto.DownstreamNameResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Service
public class DownstreamAggregationService {

	private final RestClient restClient;
	private final String aggregationPath;
	private final String defaultName;

	public DownstreamAggregationService(
		RestClient.Builder restClientBuilder,
		@Value("${downstream.base-url:http://18.216.74.156:8080}") String baseUrl,
		@Value("${downstream.aggregation-path:/name/aggregation}") String aggregationPath,
		@Value("${downstream.default-name:Steven}") String defaultName
	) {
		this.restClient = restClientBuilder.baseUrl(baseUrl).build();
		this.aggregationPath = aggregationPath;
		this.defaultName = defaultName;
	}

	public DownstreamNameResponse aggregateDefaultName() {
		return aggregateName(defaultName);
	}

	public DownstreamNameResponse aggregateName(String name) {
		String nameToSend = StringUtils.hasText(name) ? name.trim() : defaultName;
		try {
			return restClient.post()
				.uri(aggregationPath)
				.contentType(MediaType.APPLICATION_JSON)
				.body(new DownstreamNameRequest(nameToSend))
				.retrieve()
				.body(DownstreamNameResponse.class);
		} catch (RestClientException ex) {
			throw new ResponseStatusException(BAD_GATEWAY, "Failed to call downstream aggregation service", ex);
		}
	}

}
