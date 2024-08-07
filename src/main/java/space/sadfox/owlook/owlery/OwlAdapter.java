package space.sadfox.owlook.owlery;

import java.util.UUID;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.utils.Owlook;

public abstract class OwlAdapter<T extends OwlEntity> extends XmlAdapter<String, Owl<T>> {

  @Override
  public Owl<T> unmarshal(String v) throws Exception {
    try {
      UUID uuid = UUID.fromString(v);
      return OwlLoader.INSTANCE.loadOwl(uuid, getTarget());
    } catch (Exception e) {
      Owlook.registerException(e);
      Owlook.notificate(OwlLoader.generateMessage(e));
      throw e;
    }
  }

  @Override
  public String marshal(Owl<T> v) throws Exception {
    return v.info().id().toString();
  }

  protected abstract Class<T> getTarget();

}
