package space.sadfox.owlook.owlery;

import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.owlery.OwlReferenceBase.State;

public class OwlReferenceListAdapter extends OwlReferenceBaseAdapter<OwlReferenceList<?>> {

  @Override
  protected OwlReferenceList<?> createInstance(Class<? extends OwlEntity> targetOwlEntity,
      State state) {
    return new OwlReferenceList<>(targetOwlEntity, state);
  }

}
