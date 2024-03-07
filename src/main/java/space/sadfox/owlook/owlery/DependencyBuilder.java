package space.sadfox.owlook.owlery;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntityHasNoContainingOwls;

public class DependencyBuilder {
  public static class Dependency {
    private final Owl<?> owl;
    private final Set<Owl<?>> dependencyFor = new HashSet<>();
    private final Set<Owl<?>> dependsOn = new HashSet<>();

    private Dependency(Owl<?> owl) {
      this.owl = owl;

    }

    public Owl<?> getOwl() {
      return owl;
    }

    public Set<Owl<?>> getDependencyFor() {
      return Collections.unmodifiableSet(dependencyFor);
    }

    public Set<Owl<?>> getDependsOn() {
      return Collections.unmodifiableSet(dependsOn);
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      Function<Owl<?>, String> owlToString = owl -> {
        return owl.info().owlName() + ": " + owl.head().getTitle() + " [" + owl.info().id() + "]";
      };
      String t = "\t";
      String tt = "\t\t";

      builder.append(owlToString.apply(owl)).append('\n');
      builder.append(t).append("DependsOn:\n");
      dependsOn.forEach(owl -> {
        builder.append(tt).append(owlToString.apply(owl)).append('\n');
      });

      builder.append(t).append("DependsencyFor:\n");
      dependencyFor.forEach(owl -> {
        builder.append(tt).append(owlToString.apply(owl)).append('\n');
      });
      return builder.toString();

    }

    @Override
    public boolean equals(Object obj) {
      return owl.equals(obj);
    }

    @Override
    public int hashCode() {
      return owl.hashCode();
    }

  }

  private final Map<Owl<?>, Dependency> dependencies = new HashMap<>();

  public DependencyBuilder(List<Owl<?>> owls) {
    owls.forEach(owl -> {
      if (!dependencies.containsKey(owl)) {
        dependencies.put(owl, new Dependency(owl));
      }
    });

    dependencies.forEach((owl, dependency) -> {
      try {
        List<Owl<?>> owlChildrens = owl.entity().getChildrenOwls();
        if (owlChildrens == null || owlChildrens.size() == 0) {
          throw new OwlEntityHasNoContainingOwls();
        }
        dependency.dependsOn.addAll(owlChildrens);
        owlChildrens.forEach(owlChildren -> {
          if (dependencies.containsKey(owlChildren)) {
            dependencies.get(owlChildren).dependencyFor.add(owl);
          }
        });
      } catch (OwlEntityHasNoContainingOwls e) {
      }

    });
  }

  public Set<Owl<?>> getForgottenOwls() {
    Set<Owl<?>> forgottenOwls = new HashSet<>();

    dependencies.forEach((owl, dep) -> {
      boolean hide = owl.head().isHide();
      boolean notDependencyFor = dep.getDependencyFor().size() == 0;
      if (hide && notDependencyFor) {
        forgottenOwls.add(owl);
      }
    });
    return forgottenOwls;
  }

  public int getDendencyForSize(Owl<?> owl) {
    if (!dependencies.containsKey(owl)) {
      return 0;
    }
    return dependencies.get(owl).getDependencyFor().size();
  }

  public int getDependsOnSize(Owl<?> owl) {
    if (!dependencies.containsKey(owl)) {
      return 0;
    }
    return dependencies.get(owl).getDependsOn().size();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    dependencies.forEach((owl, dep) -> {
      builder.append(dep).append('\n');
    });
    return builder.toString();
  }

  public Optional<Dependency> getDendency(Owl<?> owl) {
    Optional<Dependency> dependency = null;
    if (dependencies.containsKey(owl)) {
      dependency = Optional.of(dependencies.get(owl));
    } else {
      dependency = Optional.empty();
    }
    return dependency;
  }
}
