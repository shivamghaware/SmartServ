package com.smartserv.exceptions;

@SuppressWarnings("serial")
public class ResourceAlreadyExists extends RuntimeException {
	public ResourceAlreadyExists(String mesg) {
		super(mesg);
	}
}
