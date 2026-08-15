package com.smartserv.exceptions;

@SuppressWarnings("serial")
public class UserNotFoundException extends RuntimeException{
	public UserNotFoundException(String mesg) {
		super(mesg);
	}

}
