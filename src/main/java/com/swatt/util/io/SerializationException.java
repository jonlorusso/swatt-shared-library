package com.swatt.util.io;

import java.io.IOException;

public class SerializationException extends IOException {
	private static final long	serialVersionUID	= 1L;
	
	private Throwable cause;
	
	public SerializationException() {
		super();
	}

	public SerializationException(String message) {
		super(message);
	}

	public SerializationException(String message, Throwable throwable) {
		super(message);
		this.cause = throwable;
	}
	
	public Throwable getCause() { return cause; }

}
