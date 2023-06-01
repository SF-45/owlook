package space.sadfox.owlook.utils;

public class Nullable extends Exception {

	private static final long serialVersionUID = 1L;

	public Nullable() {
		super();
	}

	public Nullable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public Nullable(String message, Throwable cause) {
		super(message, cause);
	}

	public Nullable(String message) {
		super(message);
	}

	public Nullable(Throwable cause) {
		super(cause);
	}

}
