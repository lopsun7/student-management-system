package com.studentmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "downstream")
public class DownstreamProperties {

	private String baseUrl;
	private String aggregationPath;
	private String defaultName;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getAggregationPath() {
		return aggregationPath;
	}

	public void setAggregationPath(String aggregationPath) {
		this.aggregationPath = aggregationPath;
	}

	public String getDefaultName() {
		return defaultName;
	}

	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

}
