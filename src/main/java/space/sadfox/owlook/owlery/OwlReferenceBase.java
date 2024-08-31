package space.sadfox.owlook.owlery;

import java.util.ArrayList;
import java.util.List;
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
    @XmlAttribute
    boolean removeUnloadOwlIds = false;

    public State() {
      owlInfoList = new ArrayList<>();
    }

    public State(List<OwlInfo> owlInfoList) {
      this.owlInfoList = owlInfoList;
    }

    @Override
    public String toString() {
      return "State{owlInfoList=" + owlInfoList + ", targetClassName=" + targetClassName
          + ", removeUnloadOwlIds=" + removeUnloadOwlIds + "}";
    }
  }

  protected final Class<T> targetOwlEntity;
  protected final List<OwlInfo> owlInfoList;
  protected final boolean removeUnloadOwlIds;

  public OwlReferenceBase(Class<T> targetOwlEntity) {
    this.targetOwlEntity = targetOwlEntity;
    this.owlInfoList = new ArrayList<>();
    this.removeUnloadOwlIds = false;

  }

  OwlReferenceBase(Class<T> targetOwlEntity, List<OwlInfo> owlInfoList,
      boolean removeUnloadOwlIds) {
    this.targetOwlEntity = targetOwlEntity;

    this.owlInfoList = owlInfoList;
    this.removeUnloadOwlIds = removeUnloadOwlIds;
  }

  State toState() {
    State state = new State(owlInfoList);
    state.targetClassName = targetOwlEntity.getName();
    state.removeUnloadOwlIds = removeUnloadOwlIds;
    return state;
  }

  abstract List<Owl<? extends OwlEntity>> owls();


}


