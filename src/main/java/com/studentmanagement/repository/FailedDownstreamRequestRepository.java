package com.studentmanagement.repository;

import com.studentmanagement.model.FailedDownstreamRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class FailedDownstreamRequestRepository {

	private static final RowMapper<FailedDownstreamRequest> FAILED_REQUEST_ROW_MAPPER = (rs, rowNum) -> {
		FailedDownstreamRequest request = new FailedDownstreamRequest();
		request.setId(rs.getLong("id"));
		request.setAggregatedName(rs.getString("aggregated_name"));
		request.setStatus(rs.getString("status"));
		request.setFailureReason(rs.getString("failure_reason"));
		request.setAttemptCount(rs.getInt("attempt_count"));
		request.setRecoveredResponse(rs.getString("recovered_response"));
		Timestamp createdAt = rs.getTimestamp("created_at");
		if (createdAt != null) {
			request.setCreatedAt(createdAt.toLocalDateTime());
		}
		Timestamp updatedAt = rs.getTimestamp("updated_at");
		if (updatedAt != null) {
			request.setUpdatedAt(updatedAt.toLocalDateTime());
		}
		return request;
	};

	private final JdbcTemplate jdbcTemplate;

	public FailedDownstreamRequestRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Long savePending(String aggregatedName, String failureReason) {
		LocalDateTime now = LocalDateTime.now();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			var statement = connection.prepareStatement(
				"""
				INSERT INTO failed_downstream_requests
					(aggregated_name, status, failure_reason, attempt_count, recovered_response, created_at, updated_at)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""",
				new String[] { "id" }
			);
			statement.setString(1, aggregatedName);
			statement.setString(2, "PENDING");
			statement.setString(3, failureReason);
			statement.setInt(4, 0);
			statement.setString(5, null);
			statement.setTimestamp(6, Timestamp.valueOf(now));
			statement.setTimestamp(7, Timestamp.valueOf(now));
			return statement;
		}, keyHolder);
		Number key = keyHolder.getKey();
		return key == null ? -1L : key.longValue();
	}

	public Optional<FailedDownstreamRequest> findById(Long id) {
		List<FailedDownstreamRequest> requests = jdbcTemplate.query(
			"""
			SELECT id, aggregated_name, status, failure_reason, attempt_count, recovered_response, created_at, updated_at
			FROM failed_downstream_requests
			WHERE id = ?
			""",
			FAILED_REQUEST_ROW_MAPPER,
			id
		);
		return requests.stream().findFirst();
	}

	public void markRecovered(Long id, String recoveredResponse, int attemptCount) {
		jdbcTemplate.update(
			"""
			UPDATE failed_downstream_requests
			SET status = ?, recovered_response = ?, attempt_count = ?, updated_at = ?
			WHERE id = ?
			""",
			"RECOVERED",
			recoveredResponse,
			attemptCount,
			Timestamp.valueOf(LocalDateTime.now()),
			id
		);
	}

	public void markFailed(Long id, String failureReason, int attemptCount) {
		jdbcTemplate.update(
			"""
			UPDATE failed_downstream_requests
			SET status = ?, failure_reason = ?, attempt_count = ?, updated_at = ?
			WHERE id = ?
			""",
			"FAILED",
			failureReason,
			attemptCount,
			Timestamp.valueOf(LocalDateTime.now()),
			id
		);
	}

}
