package com.studentmanagement.service;

import java.util.concurrent.CompletableFuture;

public interface DownstreamRecoveryService {

	CompletableFuture<Void> retryFailedRequestAsync(Long requestId);

}
