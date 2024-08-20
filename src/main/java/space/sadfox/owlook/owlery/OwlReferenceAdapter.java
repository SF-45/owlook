package space.sadfox.owlook.owlery;

import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.owlery.OwlReferenceBase.State;

public class OwlReferenceAdapter extends OwlReferenceBaseAdapter<OwlReference<?>> {

  @Override
  protected OwlReference<?> createInstance(Class<? extends OwlEntity> targetOwlEntity,
      State state) {
    return new OwlReference<>(targetOwlEntity, state);
  }


}
