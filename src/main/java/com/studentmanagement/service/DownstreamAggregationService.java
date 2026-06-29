package com.studentmanagement.service;

import com.studentmanagement.dto.DownstreamNameResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DownstreamAggregationService {

	private final String defaultName;

	public DownstreamAggregationService(@Value("${downstream.default-name:Steven}") String defaultName) {
		this.defaultName = defaultName;
	}

	public DownstreamNameResponse aggregateDefaultName() {
		return new DownstreamNameResponse(defaultName);
	}

	public DownstreamNameResponse aggregateName(String names) {
		String normalizedNames = StringUtils.hasText(names) ? names.trim() : "";
		if (normalizedNames.isEmpty()) {
			return aggregateDefaultName();
		}
		return new DownstreamNameResponse(defaultName + ", " + normalizedNames);
	}

}
