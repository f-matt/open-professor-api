package com.github.fmatt.openprofessor.utils;

import java.util.logging.Logger;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

@Dependent
public class LoggerProducer {
	
	@Produces
	private Logger createLogger(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint
				.getMember()
				.getDeclaringClass()
				.getName());
	}

}
