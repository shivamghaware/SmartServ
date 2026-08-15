package com.smartserv.exceptions;

@SuppressWarnings("serial")
public class DuplicateJobCreationException extends RuntimeException {
	public DuplicateJobCreationException(String mesg) {

		super(mesg);
	}
}
