package space.sadfox.owlook.jaxb;


public class ControllerNotDefined extends Exception {

	private static final long serialVersionUID = 1L;

	public ControllerNotDefined() {
		super();
	}

	public ControllerNotDefined(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ControllerNotDefined(String message, Throwable cause) {
		super(message, cause);
	}

	public ControllerNotDefined(String message) {
		super(message);
	}

	public ControllerNotDefined(Throwable cause) {
		super(cause);
	}

}
