package space.sadfox.owlook.owlery;

import java.util.List;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.base.owl.OwlInfo;

public class OwlReferenceListAdapter extends OwlReferenceBaseAdapter<OwlReferenceList<?>> {
  @Override
  protected OwlReferenceList<?> createInstance(Class<? extends OwlEntity> targetOwlEntity,
      List<OwlInfo> owlInfoList, boolean removeUnloadOwlIds) {
    return new OwlReferenceList<>(targetOwlEntity, owlInfoList, removeUnloadOwlIds);
  }
}
