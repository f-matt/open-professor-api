package com.github.fmatt.openprofessor.utils;

import jakarta.ejb.ApplicationException;

@ApplicationException
public class CustomRuntimeException extends RuntimeException {
    
	public CustomRuntimeException(String message) {
		super(message);
	}

}
