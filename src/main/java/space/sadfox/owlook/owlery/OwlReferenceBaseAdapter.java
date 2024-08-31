package space.sadfox.owlook.owlery;

import java.util.List;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.base.owl.OwlInfo;
import space.sadfox.owlook.owlery.OwlReferenceBase.State;

public abstract class OwlReferenceBaseAdapter<T extends OwlReferenceBase<?>>
    extends XmlAdapter<State, T> {

  @Override
  public T unmarshal(State v) throws Exception {
    var optTarget = OwlLoader.findTarget(v.targetClassName);
    if (optTarget.isEmpty()) {
      throw new ClassNotFoundException("Provider was not found: " + v.targetClassName);
    }
    return createInstance(optTarget.get(), v.owlInfoList, v.removeUnloadOwlIds);

  }

  @Override
  public State marshal(T v) throws Exception {
    return v.toState();
  }

  protected abstract T createInstance(Class<? extends OwlEntity> targetOwlEntity, List<OwlInfo> owlInfoList,
      boolean removeUnloadOwlIds); 
      
}

