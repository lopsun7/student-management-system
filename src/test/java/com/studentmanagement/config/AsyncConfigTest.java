package com.studentmanagement.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class AsyncConfigTest {

	private final AsyncConfig asyncConfig = new AsyncConfig();

	@Test
	void shouldCreateStudentTaskExecutorWithConfiguredPoolSizes() {
		Executor executor = asyncConfig.studentTaskExecutor(2, 4, 50);

		assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
		ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
		assertThat(taskExecutor.getCorePoolSize()).isEqualTo(2);
		assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(4);
		assertThat(taskExecutor.getThreadNamePrefix()).isEqualTo("student-async-");
		taskExecutor.shutdown();
	}

	@Test
	void shouldCreateIntegrationRecoveryTaskExecutorWithConfiguredPoolSizes() {
		Executor executor = asyncConfig.integrationRecoveryTaskExecutor(1, 2, 20);

		assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
		ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
		assertThat(taskExecutor.getCorePoolSize()).isEqualTo(1);
		assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(2);
		assertThat(taskExecutor.getThreadNamePrefix()).isEqualTo("integration-recovery-");
		taskExecutor.shutdown();
	}
}
