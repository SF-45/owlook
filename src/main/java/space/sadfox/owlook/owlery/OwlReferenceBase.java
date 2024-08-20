package space.sadfox.owlook.owlery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.base.owl.UUIDXmlAdapter;

public abstract class OwlReferenceBase<T extends OwlEntity> {
  public static class State {
    @XmlElement(name = "owlId")
    @XmlJavaTypeAdapter(UUIDXmlAdapter.class)
    final List<UUID> owlIds = new ArrayList<>();
    @XmlAttribute
    String targetClassName = "";
    @XmlAttribute
    boolean removeUnloadOwlIds = false;

    @Override
    public String toString() {
      return "State{owlIds=" + owlIds + ", targetClassName=" + targetClassName
          + ", removeUnloadOwlIds=" + removeUnloadOwlIds + "}";
    }
  }

  protected final Class<T> targetOwlEntity;
  protected final State state;

  public OwlReferenceBase(Class<T> targetOwlEntity) {
    this.targetOwlEntity = targetOwlEntity;
    this.state = new State();
    this.state.targetClassName = targetOwlEntity.getName();

  }

  OwlReferenceBase(Class<T> targetOwlEntity, State state) {
    this.targetOwlEntity = targetOwlEntity;
    this.state = state;
  }

  abstract List<Owl<? extends OwlEntity>> owls();


}


