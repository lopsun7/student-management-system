package com.studentmanagement.service;

import com.studentmanagement.dto.UpstreamNameResponse;
import java.util.List;

public interface DownstreamAggregationService {

	UpstreamNameResponse aggregateDefaultName();

	UpstreamNameResponse aggregateNames(List<String> names);

}
