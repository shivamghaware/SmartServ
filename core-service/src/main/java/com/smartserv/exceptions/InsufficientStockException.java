package com.smartserv.exceptions;

@SuppressWarnings("serial")
public class InsufficientStockException extends RuntimeException {
	public InsufficientStockException(String mesg) {
		super(mesg);
	}
}
