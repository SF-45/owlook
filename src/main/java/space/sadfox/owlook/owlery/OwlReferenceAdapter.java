package space.sadfox.owlook.owlery;

import java.util.List;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.base.owl.OwlInfo;

public class OwlReferenceAdapter extends OwlReferenceBaseAdapter<OwlReference<?>> {
  @Override
  protected OwlReference<?> createInstance(Class<? extends OwlEntity> targetOwlEntity,
      List<OwlInfo> owlInfoList) {
    return new OwlReference<>(targetOwlEntity, owlInfoList);
  }
}
