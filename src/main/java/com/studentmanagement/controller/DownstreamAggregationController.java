package com.studentmanagement.controller;

import com.studentmanagement.dto.DownstreamNameRequest;
import com.studentmanagement.dto.DownstreamNameResponse;
import com.studentmanagement.service.DownstreamAggregationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations")
public class DownstreamAggregationController {

	private final DownstreamAggregationService downstreamAggregationService;

	public DownstreamAggregationController(DownstreamAggregationService downstreamAggregationService) {
		this.downstreamAggregationService = downstreamAggregationService;
	}

	@PostMapping("/name/aggregation")
	public DownstreamNameResponse aggregateName(@RequestBody(required = false) DownstreamNameRequest request) {
		if (request == null) {
			return downstreamAggregationService.aggregateDefaultName();
		}
		return downstreamAggregationService.aggregateName(request.name());
	}

}
