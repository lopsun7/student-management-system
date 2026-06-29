package com.studentmanagement.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.dto.UpstreamNameResponse;
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
			.thenReturn(new UpstreamNameResponse(java.util.List.of("Steven", "Jessica", "Krystal", "Celine")));

		mockMvc.perform(post("/api/v1/integrations/name/aggregation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, java.util.List<String>>() {{
					put("name", java.util.List.of("Jessica", "Krystal"));
				}})))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name[0]").value("Steven"))
			.andExpect(jsonPath("$.name[1]").value("Jessica"))
			.andExpect(jsonPath("$.name[2]").value("Krystal"))
			.andExpect(jsonPath("$.name[3]").value("Celine"));

		verify(downstreamAggregationService).aggregateNames(java.util.List.of("Jessica", "Krystal"));
	}

	@Test
	void shouldAggregateDefaultNameWhenRequestBodyMissing() throws Exception {
		when(downstreamAggregationService.aggregateDefaultName())
			.thenReturn(new UpstreamNameResponse(java.util.List.of("Steven", "Celine")));

		mockMvc.perform(post("/api/v1/integrations/name/aggregation"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name[0]").value("Steven"))
			.andExpect(jsonPath("$.name[1]").value("Celine"));

		verify(downstreamAggregationService).aggregateDefaultName();
	}

	@Test
	void shouldAggregateDefaultNameWhenRequestArrayIsEmpty() throws Exception {
		when(downstreamAggregationService.aggregateDefaultName())
			.thenReturn(new UpstreamNameResponse(java.util.List.of("Steven", "Celine")));

		mockMvc.perform(post("/api/v1/integrations/name/aggregation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, java.util.List<String>>() {{
					put("name", java.util.List.of());
				}})))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name[0]").value("Steven"))
			.andExpect(jsonPath("$.name[1]").value("Celine"));

		verify(downstreamAggregationService).aggregateDefaultName();
	}

}
