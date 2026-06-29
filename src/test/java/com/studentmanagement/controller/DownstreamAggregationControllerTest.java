package com.studentmanagement.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.dto.DownstreamNameResponse;
import com.studentmanagement.service.DownstreamAggregationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DownstreamAggregationController.class)
class DownstreamAggregationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private DownstreamAggregationService downstreamAggregationService;

	@Test
	void shouldAggregateProvidedNames() throws Exception {
		when(downstreamAggregationService.aggregateNames(java.util.List.of("Jessica", "Krystal")))
			.thenReturn(new DownstreamNameResponse("Steven, Jessica, Krystal, Celine"));

		mockMvc.perform(post("/api/v1/integrations/name/aggregation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, java.util.List<String>>() {{
					put("name", java.util.List.of("Jessica", "Krystal"));
				}})))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Steven, Jessica, Krystal, Celine"));

		verify(downstreamAggregationService).aggregateNames(java.util.List.of("Jessica", "Krystal"));
	}

	@Test
	void shouldAggregateDefaultNameWhenRequestBodyMissing() throws Exception {
		when(downstreamAggregationService.aggregateDefaultName())
			.thenReturn(new DownstreamNameResponse("Steven, Celine"));

		mockMvc.perform(post("/api/v1/integrations/name/aggregation"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Steven, Celine"));

		verify(downstreamAggregationService).aggregateDefaultName();
	}

	@Test
	void shouldAggregateDefaultNameWhenRequestArrayIsEmpty() throws Exception {
		when(downstreamAggregationService.aggregateDefaultName())
			.thenReturn(new DownstreamNameResponse("Steven, Celine"));

		mockMvc.perform(post("/api/v1/integrations/name/aggregation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, java.util.List<String>>() {{
					put("name", java.util.List.of());
				}})))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Steven, Celine"));

		verify(downstreamAggregationService).aggregateDefaultName();
	}

}
