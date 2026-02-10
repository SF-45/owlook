package space.sadfox.owlook.owlery;

public class OwlNotFoundException extends OwlException {

  private static final long serialVersionUID = 3048469620722169768L;

  public OwlNotFoundException() {
    super();
  }

  public OwlNotFoundException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public OwlNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public OwlNotFoundException(String message) {
    super(message);
  }

  public OwlNotFoundException(Throwable cause) {
    super(cause);
  }

}
