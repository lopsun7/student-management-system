package com.studentmanagement.controller;

import com.studentmanagement.dto.DownstreamNameResponse;
import com.studentmanagement.dto.UpstreamNameRequest;
import com.studentmanagement.dto.UpstreamNameResponse;
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
	public UpstreamNameResponse aggregateName(@RequestBody(required = false) UpstreamNameRequest request) {
		if (request == null || request.name() == null || request.name().isEmpty()) {
			return downstreamAggregationService.aggregateDefaultName();
		}
		return downstreamAggregationService.aggregateNames(request.name());
	}

}
