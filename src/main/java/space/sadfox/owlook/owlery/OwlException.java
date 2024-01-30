package space.sadfox.owlook.owlery;

public class OwlException extends Exception {

	private static final long serialVersionUID = 1L;

	public OwlException() {
		super();
	}

	public OwlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public OwlException(String message, Throwable cause) {
		super(message, cause);
	}

	public OwlException(String message) {
		super(message);
	}

	public OwlException(Throwable cause) {
		super(cause);
	}

}
