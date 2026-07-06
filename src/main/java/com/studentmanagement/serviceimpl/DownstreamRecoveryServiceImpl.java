package com.studentmanagement.serviceimpl;

import com.studentmanagement.client.DownstreamAggregationClient;
import com.studentmanagement.config.DownstreamProperties;
import com.studentmanagement.dto.DownstreamNameResponse;
import com.studentmanagement.model.FailedDownstreamRequest;
import com.studentmanagement.repository.FailedDownstreamRequestRepository;
import com.studentmanagement.service.DownstreamRecoveryService;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DownstreamRecoveryServiceImpl implements DownstreamRecoveryService {

	private static final String DOWNSTREAM_AGGREGATION = "downstreamAggregation";

	private final DownstreamAggregationClient downstreamAggregationClient;
	private final FailedDownstreamRequestRepository failedDownstreamRequestRepository;
	private final int maxAttempts;

	public DownstreamRecoveryServiceImpl(
			DownstreamAggregationClient downstreamAggregationClient,
			FailedDownstreamRequestRepository failedDownstreamRequestRepository,
			@Value("${resilience4j.retry.instances.downstreamAggregation.max-attempts:3}") int maxAttempts) {
		this.downstreamAggregationClient = downstreamAggregationClient;
		this.failedDownstreamRequestRepository = failedDownstreamRequestRepository;
		this.maxAttempts = maxAttempts;
	}

	@Override
	@Async("integrationRecoveryTaskExecutor")
	@CircuitBreaker(name = DOWNSTREAM_AGGREGATION)
	@Retry(name = DOWNSTREAM_AGGREGATION, fallbackMethod = "markRecoveryFailed")
	public CompletableFuture<Void> retryFailedRequestAsync(Long requestId) {
		if (requestId == null || requestId < 0) {
			return CompletableFuture.completedFuture(null);
		}
		Optional<FailedDownstreamRequest> requestOptional = failedDownstreamRequestRepository.findById(requestId);
		if (requestOptional.isEmpty()) {
			return CompletableFuture.completedFuture(null);
		}

		FailedDownstreamRequest request = requestOptional.get();
		DownstreamNameResponse response = downstreamAggregationClient.aggregate(request.getAggregatedName());
		String recoveredResponse = response == null || !StringUtils.hasText(response.name())
			? request.getAggregatedName()
			: response.name();
		failedDownstreamRequestRepository.markRecovered(
			requestId,
			recoveredResponse,
			maxAttempts
		);
		return CompletableFuture.completedFuture(null);
	}

	private CompletableFuture<Void> markRecoveryFailed(Long requestId, Throwable exception) {
		if (requestId != null && requestId >= 0) {
			failedDownstreamRequestRepository.markFailed(requestId, exception.getMessage(), maxAttempts);
		}
		return CompletableFuture.completedFuture(null);
	}

}
