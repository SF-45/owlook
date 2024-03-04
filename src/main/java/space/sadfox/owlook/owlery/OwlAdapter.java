package space.sadfox.owlook.owlery;

import java.util.UUID;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.utils.MessageLevel;
import space.sadfox.owlook.utils.Owlook;
import space.sadfox.owlook.utils.OwlookMessage;

public abstract class OwlAdapter<T extends OwlEntity> extends XmlAdapter<String, Owl<T>> {

  @Override
  public Owl<T> unmarshal(String v) throws Exception {
    try {
      UUID uuid = UUID.fromString(v);
      return OwlLoader.INSTANCE.loadOwl(uuid, getTarget());
    } catch (OwlNotFoundException e) {
      Owlook.registerException(1, e);
      OwlookMessage message = new OwlookMessage(MessageLevel.WARNING);
      message.setName("Owl Not Found [" + v + "]");
      message.setMessage(e.getMessage());
      Owlook.registerMessage(message);
      Owlook.notificate(message);
      throw e;
    } catch (Exception e) {
      Owlook.registerException(1, e);
      OwlookMessage message = new OwlookMessage(MessageLevel.WARNING);
      message.setName("Owl Not loaded [" + v + "]");
      message.setMessage(e.getMessage());
      Owlook.registerMessage(message);
      Owlook.notificate(message);
      throw e;
    }
  }

  @Override
  public String marshal(Owl<T> v) throws Exception {
    return v.info().id().toString();
  }

  protected abstract Class<T> getTarget();

}
