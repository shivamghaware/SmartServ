package com.smartserv.exceptions;

@SuppressWarnings("serial")

public class JobCardNotFoundException extends RuntimeException{
	public JobCardNotFoundException(String mesg) {
		super(mesg);
	}
}
