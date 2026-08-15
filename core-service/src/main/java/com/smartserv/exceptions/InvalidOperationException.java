package com.smartserv.exceptions;

@SuppressWarnings("serial")
public class InvalidOperationException extends RuntimeException {
	public InvalidOperationException(String mesg) {
		super(mesg);
	}
}
