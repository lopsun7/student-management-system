package com.studentmanagement.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DownstreamPropertiesTest {

	@Test
	void shouldStoreDownstreamConfigurationValues() {
		DownstreamProperties properties = new DownstreamProperties();

		properties.setBaseUrl("http://localhost:8080");
		properties.setAggregationPath("/name/aggregation");
		properties.setDefaultName("Steven");

		assertThat(properties.getBaseUrl()).isEqualTo("http://localhost:8080");
		assertThat(properties.getAggregationPath()).isEqualTo("/name/aggregation");
		assertThat(properties.getDefaultName()).isEqualTo("Steven");
	}
}
