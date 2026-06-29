package com.studentmanagement.service;

import com.studentmanagement.dto.DownstreamNameRequest;
import com.studentmanagement.dto.DownstreamNameResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class DownstreamAggregationService {

	private final RestClient restClient;
	private final String aggregationPath;
	private final String defaultName;

	public DownstreamAggregationService(RestClient.Builder restClientBuilder,
			@Value("${downstream.base-url:http://18.217.46.236:8080}") String downstreamBaseUrl,
			@Value("${downstream.aggregation-path:/name/aggregation}") String aggregationPath,
			@Value("${downstream.default-name:Steven}") String defaultName) {
		this.restClient = restClientBuilder.baseUrl(downstreamBaseUrl).build();
		this.aggregationPath = aggregationPath;
		this.defaultName = defaultName;
	}

	public DownstreamNameResponse aggregateDefaultName() {
		return forwardToDownstream(defaultName);
	}

	public DownstreamNameResponse aggregateNames(List<String> names) {
		String normalizedNames = normalizeNames(names);
		if (normalizedNames.isEmpty()) {
			return aggregateDefaultName();
		}
		return forwardToDownstream(defaultName + ", " + normalizedNames);
	}

	private String normalizeNames(List<String> names) {
		if (names == null || names.isEmpty()) {
			return "";
		}
		return names.stream()
			.filter(StringUtils::hasText)
			.map(String::trim)
			.filter(StringUtils::hasText)
			.reduce((left, right) -> left + ", " + right)
			.orElse("");
	}

	private DownstreamNameResponse forwardToDownstream(String aggregatedName) {
		DownstreamNameResponse downstreamResponse = restClient.post()
			.uri(aggregationPath)
			.contentType(MediaType.APPLICATION_JSON)
			.body(new DownstreamNameRequest(aggregatedName))
			.retrieve()
			.body(DownstreamNameResponse.class);
		if (downstreamResponse == null || !StringUtils.hasText(downstreamResponse.name())) {
			return new DownstreamNameResponse(aggregatedName);
		}
		return downstreamResponse;
	}

}
