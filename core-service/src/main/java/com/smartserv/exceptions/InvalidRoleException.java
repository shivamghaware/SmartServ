package com.smartserv.exceptions;

@SuppressWarnings("serial")
public class InvalidRoleException extends RuntimeException {
	public InvalidRoleException(String mesg) {
		super(mesg);
	}
}
