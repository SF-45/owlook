package space.sadfox.owlook.owlery;

import java.util.HashMap;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import space.sadfox.owlook.base.owl.Owl;

public enum OwlDependencies {
  INSTANCE;

  private class OwlReferencies {
    final ObservableList<Owl<?>> dependencyFor = FXCollections.observableArrayList();
    final ObservableList<Owl<?>> dependencyOn = FXCollections.observableArrayList();
  }

  private final HashMap<Owl<?>, OwlReferencies> owlReferencies = new HashMap<>();

  public int getDependencyForSize(Owl<?> owl) {
    return owlReferencies.containsKey(owl) ? owlReferencies.get(owl).dependencyFor.size() : 0;
  }

  public int getDependencyOnSize(Owl<?> owl) {
    return owlReferencies.containsKey(owl) ? owlReferencies.get(owl).dependencyOn.size() : 0;
  }

  public ObservableList<Owl<?>> getDependencyFor(Owl<?> owl) {
    ObservableList<Owl<?>> list;
    if (owlReferencies.containsKey(owl)) {
      list = owlReferencies.get(owl).dependencyFor;
    } else {
      list = FXCollections.observableArrayList();
    }
    return FXCollections.unmodifiableObservableList(list);
  }

  public ObservableList<Owl<?>> getDependencyOn(Owl<?> owl) {
    ObservableList<Owl<?>> list;
    if (owlReferencies.containsKey(owl)) {
      list = owlReferencies.get(owl).dependencyOn;
    } else {
      list = FXCollections.observableArrayList();
    }
    return FXCollections.unmodifiableObservableList(list);
  }

  synchronized void addTo(Owl<?> parent, Owl<?> child) {
    getOwlReferences(parent).dependencyOn.add(child);
    getOwlReferences(child).dependencyFor.add(parent);
  }

  synchronized void removeFrom(Owl<?> parent, Owl<?> child) {
    getOwlReferences(parent).dependencyOn.remove(child);
    getOwlReferences(child).dependencyFor.remove(parent);
    deleteIfEmpty(parent);
    deleteIfEmpty(child);
  }

  private OwlReferencies getOwlReferences(Owl<?> parent) {
    if (owlReferencies.containsKey(parent)) {
      return owlReferencies.get(parent);
    } else {
      OwlReferencies newRef = new OwlReferencies();
      owlReferencies.put(parent, newRef);
      return newRef;
    }
  }

  private void deleteIfEmpty(Owl<?> parent) {
    if (!owlReferencies.containsKey(parent)) {
      return;
    }
    OwlReferencies owlRef = getOwlReferences(parent);
    if (owlRef.dependencyFor.size() == 0 && owlRef.dependencyOn.size() == 0) {
      owlReferencies.remove(parent);
    }
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    final Function<Owl<?>, String> owlToString = owl -> {
      return owl.info().owlName() + " (" + owl.head().getTitle() + ") [" + owl.info().id() + "]";
    };
    owlReferencies.forEach((owl, dep) -> {
      b.append(owlToString.apply(owl)).append('\n');
      b.append(" DependencyFor:\n");
      dep.dependencyFor
          .forEach(depFor -> b.append("  ").append(owlToString.apply(depFor)).append('\n'));

      b.append(" DependencyOn:\n");
      dep.dependencyOn
          .forEach(depOn -> b.append("  ").append(owlToString.apply(depOn)).append('\n'));
      b.append('\n');
    });
    return b.toString();
  }
}
