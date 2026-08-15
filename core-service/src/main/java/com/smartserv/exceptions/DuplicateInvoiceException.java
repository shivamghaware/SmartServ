package com.smartserv.exceptions;

public class DuplicateInvoiceException extends RuntimeException {
	public DuplicateInvoiceException(String msg) {
		super(msg);
	}
}