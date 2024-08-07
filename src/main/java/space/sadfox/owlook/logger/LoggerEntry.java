package space.sadfox.owlook.logger;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import space.sadfox.owlook.utils.MessageLevel;

@XmlType
public class LoggerEntry {

  private String name;
  private String massage;
  private String stackTrace;
  private MessageLevel logLevel;
  private long time;

  @XmlAttribute(name = "name")
  public String getName() {
    return name;
  }

  @XmlAttribute(name = "time")
  public long getTime() {
    return time;
  }

  @XmlElement(name = "massage")
  public String getMassage() {
    return massage;
  }

  @XmlElement(name = "stackTrace")
  public String getStackTrace() {
    return stackTrace;
  }

  @XmlAttribute(name = "logLevel")
  public MessageLevel getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(MessageLevel logLevel) {
    this.logLevel = logLevel;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  public void setMessage(String message) {
    this.massage = message;
  }

  public void setTime(long time) {
    this.time = time;
  }

}
