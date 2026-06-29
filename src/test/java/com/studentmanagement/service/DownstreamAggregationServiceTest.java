package com.studentmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.studentmanagement.dto.DownstreamNameResponse;
import org.junit.jupiter.api.Test;

class DownstreamAggregationServiceTest {

	private final DownstreamAggregationService downstreamAggregationService =
		new DownstreamAggregationService("Steven");

	@Test
	void shouldPrefixProvidedNamesWithSteven() {
		DownstreamNameResponse response = downstreamAggregationService.aggregateName("Jessica, Amy");

		assertThat(response.name()).isEqualTo("Steven, Jessica, Amy");
	}

	@Test
	void shouldUseDefaultNameWhenInputIsBlank() {
		DownstreamNameResponse response = downstreamAggregationService.aggregateName("   ");

		assertThat(response.name()).isEqualTo("Steven");
	}

}
