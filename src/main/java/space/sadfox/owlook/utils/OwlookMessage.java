package space.sadfox.owlook.utils;

public class OwlookMessage {
  private String name;
  private String message;
  private MessageLevel messageLevel;

  public OwlookMessage(MessageLevel logLevel) {
    this.messageLevel = logLevel;
  }

  public OwlookMessage(MessageLevel logLevel, String name) {
    this.messageLevel = logLevel;
    this.name = name;
  }

  public OwlookMessage(MessageLevel logLevel, String name, String message) {
    this.messageLevel = logLevel;
    this.name = name;
    this.message = message;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public MessageLevel getMessageLevel() {
    return messageLevel;
  }

}
