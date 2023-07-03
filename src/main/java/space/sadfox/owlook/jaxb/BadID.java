package space.sadfox.owlook.jaxb;

public class BadID extends Exception {

	public BadID() {
		super();
	}

	public BadID(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BadID(String message, Throwable cause) {
		super(message, cause);
	}

	public BadID(String message) {
		super(message);
	}

	public BadID(Throwable cause) {
		super(cause);
	}

}
