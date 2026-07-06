package com.studentmanagement.serviceimpl;

import com.studentmanagement.client.DownstreamAggregationClient;
import com.studentmanagement.config.DownstreamProperties;
import com.studentmanagement.dto.DownstreamNameResponse;
import com.studentmanagement.dto.UpstreamNameResponse;
import com.studentmanagement.repository.FailedDownstreamRequestRepository;
import com.studentmanagement.service.DownstreamAggregationService;
import com.studentmanagement.service.DownstreamRecoveryService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DownstreamAggregationServiceImpl implements DownstreamAggregationService {

	private static final String DOWNSTREAM_AGGREGATION = "downstreamAggregation";

	private final DownstreamProperties downstreamProperties;
	private final DownstreamAggregationClient downstreamAggregationClient;
	private final FailedDownstreamRequestRepository failedDownstreamRequestRepository;
	private final DownstreamRecoveryService downstreamRecoveryService;

	public DownstreamAggregationServiceImpl(
			DownstreamProperties downstreamProperties,
			DownstreamAggregationClient downstreamAggregationClient,
			FailedDownstreamRequestRepository failedDownstreamRequestRepository,
			DownstreamRecoveryService downstreamRecoveryService) {
		this.downstreamProperties = downstreamProperties;
		this.downstreamAggregationClient = downstreamAggregationClient;
		this.failedDownstreamRequestRepository = failedDownstreamRequestRepository;
		this.downstreamRecoveryService = downstreamRecoveryService;
	}

	@Override
	@CircuitBreaker(name = DOWNSTREAM_AGGREGATION)
	@Retry(name = DOWNSTREAM_AGGREGATION, fallbackMethod = "fallbackDefaultName")
	public UpstreamNameResponse aggregateDefaultName() {
		return aggregateFromNormalizedNames(downstreamProperties.getDefaultName());
	}

	@Override
	@CircuitBreaker(name = DOWNSTREAM_AGGREGATION)
	@Retry(name = DOWNSTREAM_AGGREGATION, fallbackMethod = "fallbackNames")
	public UpstreamNameResponse aggregateNames(List<String> names) {
		String normalizedNames = normalizeNames(names);
		if (normalizedNames.isEmpty()) {
			return aggregateDefaultName();
		}
		return aggregateFromNormalizedNames(downstreamProperties.getDefaultName() + ", " + normalizedNames);
	}

	private UpstreamNameResponse aggregateFromNormalizedNames(String aggregatedName) {
		DownstreamNameResponse downstreamResponse = downstreamAggregationClient.aggregate(aggregatedName);
		if (downstreamResponse == null || !StringUtils.hasText(downstreamResponse.name())) {
			return toUpstreamResponse(aggregatedName);
		}
		return toUpstreamResponse(downstreamResponse.name());
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

	private UpstreamNameResponse toUpstreamResponse(String names) {
		List<String> normalizedNames = Arrays.stream(names.split(","))
			.map(String::trim)
			.filter(StringUtils::hasText)
			.toList();
		return new UpstreamNameResponse(normalizedNames);
	}

	private UpstreamNameResponse fallbackDefaultName(Throwable exception) {
		return recoverFromDownstreamFailure(downstreamProperties.getDefaultName(), exception);
	}

	private UpstreamNameResponse fallbackNames(List<String> names, Throwable exception) {
		String normalizedNames = normalizeNames(names);
		String aggregatedName = normalizedNames.isEmpty()
			? downstreamProperties.getDefaultName()
			: downstreamProperties.getDefaultName() + ", " + normalizedNames;
		return recoverFromDownstreamFailure(aggregatedName, exception);
	}

	private UpstreamNameResponse recoverFromDownstreamFailure(String aggregatedName, Throwable exception) {
		Long requestId = failedDownstreamRequestRepository.savePending(aggregatedName, exception.getMessage());
		downstreamRecoveryService.retryFailedRequestAsync(requestId);
		return toUpstreamResponse(aggregatedName);
	}

}
