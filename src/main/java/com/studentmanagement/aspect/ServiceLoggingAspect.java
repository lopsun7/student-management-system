package com.studentmanagement.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLoggingAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoggingAspect.class);

	@Pointcut("execution(* com.studentmanagement.service..*(..)) || execution(* com.studentmanagement.serviceimpl..*(..))")
	public void serviceLayer() {
	}

	@Before("serviceLayer()")
	public void logBefore(JoinPoint joinPoint) {
		LOGGER.info("AOP BEFORE -> {}", joinPoint.getSignature().toShortString());
	}

	@After("serviceLayer()")
	public void logAfter(JoinPoint joinPoint) {
		LOGGER.info("AOP AFTER -> {}", joinPoint.getSignature().toShortString());
	}

	@AfterThrowing(pointcut = "serviceLayer()", throwing = "exception")
	public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
		LOGGER.error("AOP EXCEPTION -> {} failed with {}", joinPoint.getSignature().toShortString(), exception.getMessage());
	}

	@Around("serviceLayer()")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		LOGGER.info("AOP AROUND START -> {}", joinPoint.getSignature().toShortString());
		try {
			return joinPoint.proceed();
		} finally {
			LOGGER.info("AOP AROUND END -> {}", joinPoint.getSignature().toShortString());
		}
	}
}
