package space.sadfox.owlook.owlery;

import java.util.UUID;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.utils.Owlook;
import space.sadfox.owlook.utils.LogLevel;
import space.sadfox.owlook.utils.LogMessage;

public abstract class OwlAdapter<T extends OwlEntity> extends XmlAdapter<String, Owl<T>> {

  @Override
  public Owl<T> unmarshal(String v) throws Exception {
    try {
      UUID uuid = UUID.fromString(v);
      return OwlLoader.INSTANCE.loadOwl(uuid, getTarget());
    } catch (OwlNotFoundException e) {
      Owlook.registerException(1, e);
      LogMessage message = new LogMessage(LogLevel.WARNING);
      message.setName("Owl Not Found [" + v + "]");
      message.setMessage(e.getMessage());
      message.setNotification(true);
      Owlook.registerMessage(message);
      throw e;
    } catch (Exception e) {
      Owlook.registerException(1, e);
      LogMessage message = new LogMessage(LogLevel.WARNING);
      message.setName("Owl Not loaded [" + v + "]");
      message.setMessage(e.getMessage());
      message.setNotification(true);
      Owlook.registerMessage(message);
      throw e;
    }
  }

  @Override
  public String marshal(Owl<T> v) throws Exception {
    return v.info().id().toString();
  }

  protected abstract Class<T> getTarget();

}
