package space.sadfox.owlook.logger;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import space.sadfox.owlook.base.jaxb.ObservedJAXBEntity;


@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class LoggerEntity extends ObservedJAXBEntity {

  private List<LoggerEntry> logs = new ArrayList<>();

  @XmlElement(name = "log")
  public List<LoggerEntry> getLogs() {
    return logs;
  }

  @Override
  public List<Object> getProperties() {
    return null;
  }

}
