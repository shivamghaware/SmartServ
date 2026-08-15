package com.smartserv.exceptions;

@SuppressWarnings("serial")
public class UnauthorizedException extends RuntimeException {
	public UnauthorizedException(String mesg) {
		super(mesg);
	}
}
