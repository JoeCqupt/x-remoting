package io.github.xinfra.lab.remoting.exception;

public class RemotingException extends Exception {

	public RemotingException() {
	}

	public RemotingException(Throwable cause) {
		super(cause);
	}

	public RemotingException(String message) {
		super(message);
	}

	public RemotingException(String message, Throwable cause) {
		super(message, cause);
	}

}
