package com.studentmanagement.serviceimpl;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.studentmanagement.client.DownstreamAggregationClient;
import com.studentmanagement.dto.DownstreamNameResponse;
import com.studentmanagement.model.FailedDownstreamRequest;
import com.studentmanagement.repository.FailedDownstreamRequestRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DownstreamRecoveryServiceImplTest {

	@Mock
	private DownstreamAggregationClient downstreamAggregationClient;

	@Mock
	private FailedDownstreamRequestRepository failedDownstreamRequestRepository;

	private DownstreamRecoveryServiceImpl downstreamRecoveryService;

	@BeforeEach
	void setUp() {
		downstreamRecoveryService = new DownstreamRecoveryServiceImpl(
			downstreamAggregationClient,
			failedDownstreamRequestRepository,
			3
		);
	}

	@Test
	void shouldIgnoreNullAndNegativeRequestIds() {
		downstreamRecoveryService.retryFailedRequestAsync(null).join();
		downstreamRecoveryService.retryFailedRequestAsync(-1L).join();

		verify(failedDownstreamRequestRepository, never()).findById(null);
		verify(downstreamAggregationClient, never()).aggregate(org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void shouldIgnoreMissingRequest() {
		when(failedDownstreamRequestRepository.findById(44L)).thenReturn(Optional.empty());

		downstreamRecoveryService.retryFailedRequestAsync(44L).join();

		verify(downstreamAggregationClient, never()).aggregate(org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void shouldMarkRequestRecoveredWithDownstreamResponse() {
		FailedDownstreamRequest request = new FailedDownstreamRequest();
		request.setId(7L);
		request.setAggregatedName("Steven, Jessica");
		when(failedDownstreamRequestRepository.findById(7L)).thenReturn(Optional.of(request));
		when(downstreamAggregationClient.aggregate("Steven, Jessica"))
			.thenReturn(new DownstreamNameResponse("Steven, Jessica, Celine"));

		downstreamRecoveryService.retryFailedRequestAsync(7L).join();

		verify(failedDownstreamRequestRepository).markRecovered(7L, "Steven, Jessica, Celine", 3);
	}

	@Test
	void shouldUseOriginalNameWhenDownstreamResponseIsBlank() {
		FailedDownstreamRequest request = new FailedDownstreamRequest();
		request.setId(8L);
		request.setAggregatedName("Steven");
		when(failedDownstreamRequestRepository.findById(8L)).thenReturn(Optional.of(request));
		when(downstreamAggregationClient.aggregate("Steven"))
			.thenReturn(new DownstreamNameResponse(" "));

		downstreamRecoveryService.retryFailedRequestAsync(8L).join();

		verify(failedDownstreamRequestRepository).markRecovered(8L, "Steven", 3);
	}

	@Test
	void shouldUseOriginalNameWhenDownstreamResponseIsNull() {
		FailedDownstreamRequest request = new FailedDownstreamRequest();
		request.setId(9L);
		request.setAggregatedName("Steven");
		when(failedDownstreamRequestRepository.findById(9L)).thenReturn(Optional.of(request));
		when(downstreamAggregationClient.aggregate("Steven")).thenReturn(null);

		downstreamRecoveryService.retryFailedRequestAsync(9L).join();

		verify(failedDownstreamRequestRepository).markRecovered(9L, "Steven", 3);
	}
}
