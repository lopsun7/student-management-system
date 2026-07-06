package com.studentmanagement.model;

import java.time.LocalDateTime;

public class FailedDownstreamRequest {

	private Long id;
	private String aggregatedName;
	private String status;
	private String failureReason;
	private int attemptCount;
	private String recoveredResponse;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAggregatedName() {
		return aggregatedName;
	}

	public void setAggregatedName(String aggregatedName) {
		this.aggregatedName = aggregatedName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public void setAttemptCount(int attemptCount) {
		this.attemptCount = attemptCount;
	}

	public String getRecoveredResponse() {
		return recoveredResponse;
	}

	public void setRecoveredResponse(String recoveredResponse) {
		this.recoveredResponse = recoveredResponse;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}
