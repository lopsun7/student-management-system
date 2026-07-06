package com.studentmanagement.config;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

	@Bean(name = "studentTaskExecutor")
	public Executor studentTaskExecutor(
			@Value("${async.student.core-pool-size:2}") int corePoolSize,
			@Value("${async.student.max-pool-size:4}") int maxPoolSize,
			@Value("${async.student.queue-capacity:50}") int queueCapacity) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix("student-async-");
		executor.initialize();
		return executor;
	}

	@Bean(name = "integrationRecoveryTaskExecutor")
	public Executor integrationRecoveryTaskExecutor(
			@Value("${async.integration.core-pool-size:1}") int corePoolSize,
			@Value("${async.integration.max-pool-size:2}") int maxPoolSize,
			@Value("${async.integration.queue-capacity:20}") int queueCapacity) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix("integration-recovery-");
		executor.initialize();
		return executor;
	}

}
