package space.sadfox.owlook.utils;

import java.util.function.Supplier;

public class LogSupplier {

  private final Supplier<String> supplier;

  public LogSupplier(Supplier<String> supplier) {
    this.supplier = supplier;
  }

  @Override
  public String toString() {
    return supplier.get();
  }
}
