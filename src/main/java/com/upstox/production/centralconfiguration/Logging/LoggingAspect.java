package com.upstox.production.centralconfiguration.Logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
	public static final Log LOGGER=LogFactory.getLog(LoggingAspect.class);

	@AfterThrowing(pointcut = "execution(* com.upstox..*(..))",throwing = "exception")
	public void logFromService(Exception exception)
	{
		LOGGER.error(exception.getStackTrace());
		LOGGER.error(exception.getMessage());
	}
}
