package space.sadfox.owlook.owlery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.base.owl.OwlInfo;

public abstract class OwlReferenceBase<T extends OwlEntity> {
  public static class State {
    @XmlElement(name = "owlInfo")
    List<OwlInfo> owlInfoList;
    @XmlAttribute
    String targetClassName = "";

    public State() {
      owlInfoList = new ArrayList<>();
    }

    public State(List<OwlInfo> owlInfoList) {
      this.owlInfoList = owlInfoList;
    }

    @Override
    public String toString() {
      return "State{owlInfoList=" + owlInfoList + ", targetClassName=" + targetClassName + "}";
    }

  }

  protected final Class<T> targetOwlEntity;
  protected final List<OwlInfo> owlInfoList;
  private Optional<Owl<?>> parent = Optional.empty();

  public OwlReferenceBase(Class<T> targetOwlEntity) {
    this.targetOwlEntity = targetOwlEntity;
    this.owlInfoList = new ArrayList<>();
    init();
  }

  OwlReferenceBase(Class<T> targetOwlEntity, List<OwlInfo> owlInfoList) {
    this.targetOwlEntity = targetOwlEntity;
    this.owlInfoList = owlInfoList;
    init();
  }

  protected abstract void init();

  State toState() {
    State state = new State(owlInfoList);
    state.targetClassName = targetOwlEntity.getName();
    return state;
  }

  public boolean setParent(Owl<?> parent) {
    if (this.parent.isPresent()) {
      return false;
    }

    this.parent = Optional.of(parent);
    whenParentSet();
    return true;
  }

  protected Optional<Owl<?>> getParent() {
    return parent;
  }

  protected abstract void whenParentSet();
}


