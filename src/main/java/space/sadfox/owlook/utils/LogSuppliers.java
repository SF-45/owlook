package space.sadfox.owlook.utils;

import java.util.function.Supplier;

import space.sadfox.owlook.base.owl.HollowOwl;
import space.sadfox.owlook.base.owl.Owl;

public class LogSuppliers {

  public static LogSupplier supplier(Supplier<String> supplier) {
    return new LogSupplier(supplier);
  }

  public static LogSupplier s(Supplier<String> supplier) {
    return supplier(supplier);
  }

  public static LogSupplier logOwl(Owl<?> owl) {
    return new LogSupplier(() -> {
      return String.format("%s{id=%s, title=%s, hidden=%s}",
          owl.info().owlName(),
          owl.info().id(),
          owl.head().getTitle(),
          owl.head().isHidden());
    });
  }

  public static LogSupplier logHollowOwl(HollowOwl hollowOwl) {
    return new LogSupplier(() -> {
      return String.format("%s{id=%s, filename=%s}",
          hollowOwl.info().owlName(),
          hollowOwl.info().id(),
          hollowOwl.fileName());
    });
  }
}
