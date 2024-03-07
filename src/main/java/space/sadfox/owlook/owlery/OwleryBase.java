package space.sadfox.owlook.owlery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import space.sadfox.owlook.base.moduleapi.OwlookModuleInfo;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.owlery.OwlLoader.DeleteFlag;
import space.sadfox.owlook.ui.base.Controllable;
import space.sadfox.owlook.ui.base.DesignController;
import space.sadfox.owlook.utils.MessageLevel;
import space.sadfox.owlook.utils.Owlook;
import space.sadfox.owlook.utils.OwlookMessage;

public abstract class OwleryBase extends DesignController<OwleryDesigner> {

  protected final OwleryConfig CONFIG;
  private final ObservableList<Predicate<Owl<?>>> filters = FXCollections.observableArrayList();
  private final Predicate<Owl<?>> defaultSearchPreicate;
  private Predicate<Owl<?>> customSearchPredicate;

  private final BooleanProperty listenFiltersChange = new SimpleBooleanProperty(false);
  private final BooleanProperty listenSearchChange = new SimpleBooleanProperty(false);
  private final BooleanProperty listenAddRemoveOwls = new SimpleBooleanProperty(false);

  protected OwleryBase(OwleryConfig config) {
    super(new OwleryDesigner(config));
    this.CONFIG = config;
    filters.addListener((InvalidationListener) prop -> {
      if (listenFiltersChange.get()) {
        updateVisibleOwls();
      }
    });

    DESIGN.searchOwlTextField.textProperty().addListener((property, oldValue, newValue) -> {
      if (listenSearchChange.get()) {
        updateVisibleOwls();
      }
    });

    OwlLoader.INSTANCE.addCreateOwlListener(newOwl -> {
      if (listenAddRemoveOwls.get()) {
        updateVisibleOwls();
      }
    });
    OwlLoader.INSTANCE.addDeleteOwlListener(delOwl -> {
      if (listenAddRemoveOwls.get()) {
        updateVisibleOwls();
      }
    });

    defaultSearchPreicate = owl -> {
      String searchText = DESIGN.searchOwlTextField.getText();
      if (searchText.equals("")) {
        return true;
      } else {
        searchText = searchText.toLowerCase();
        boolean title, entityName, moduleName;

        title = owl.head().getTitle().toLowerCase().contains(searchText);
        entityName = owl.info().owlName().toLowerCase().contains(searchText);
        Optional<OwlookModuleInfo> moduleInfo =
            ModuleLoader.INSTANCE.getLoadedModules(owl.info().createdModule());
        if (moduleInfo.isPresent()) {
          moduleName = moduleInfo.get().name().toLowerCase().contains(searchText);
        } else {
          moduleName = true;
        }
        return title || entityName || moduleName;
      }
    };
    addOwlsFilter(defaultSearchPreicate);
  }

  protected void updateVisibleOwls() {
    List<Owl<?>> owls = OwlLoader.INSTANCE.getOwls();
    if (filters.size() > 0) {
      Predicate<Owl<?>> rezultFilter = null;
      for (Predicate<Owl<?>> filter : filters) {
        rezultFilter = rezultFilter == null ? filter : rezultFilter.and(filter);
      }
      owls = owls.stream().filter(rezultFilter).collect(Collectors.toList());
    }
    ObservableList<Owl<?>> visibleOwls = DESIGN.owlTableView.getItems();
    visibleOwls.clear();
    visibleOwls.addAll(owls);
  }

  protected boolean isListenFiltersChange() {
    return listenFiltersChange.get();
  }

  protected void setListenFiltersChange(boolean listenFiltersChange) {
    this.listenFiltersChange.set(listenFiltersChange);
  }

  public void addOwlsFilter(Predicate<Owl<?>> owlFilter) {
    filters.add(owlFilter);
  }

  public boolean removeOwlsFilter(Predicate<Owl<?>> owlFilter) {
    return filters.remove(owlFilter);
  }

  protected void setCustomSearchPredicate(Predicate<Owl<?>> searchPredicate) {
    if (customSearchPredicate == null && searchPredicate != null) {
      removeOwlsFilter(defaultSearchPreicate);
      customSearchPredicate = searchPredicate;
      addOwlsFilter(searchPredicate);
    } else if (customSearchPredicate != null) {
      removeOwlsFilter(customSearchPredicate);
      customSearchPredicate = searchPredicate;
      if (searchPredicate == null) {
        addOwlsFilter(defaultSearchPreicate);
      } else {
        addOwlsFilter(searchPredicate);
      }
    }
  }

  protected boolean isListenSearchChange() {
    return listenSearchChange.get();
  }

  protected void setListenSearchChange(boolean listenSearchChange) {
    this.listenSearchChange.set(listenSearchChange);
  }

  public boolean isListenAddRemoveOwls() {
    return listenAddRemoveOwls.get();
  }

  public void setListenAddRemoveOwls(boolean listenAddRemoveOwls) {
    this.listenAddRemoveOwls.set(listenAddRemoveOwls);
  }

  protected static void createOwlAction(Class<? extends OwlEntity> target) {
    try {
      Owl<?> newOwl = OwlLoader.INSTANCE.createOwl(target);
      editOwlAction(newOwl);
    } catch (Exception e) {
      Owlook.registerException(0, e);
    }
  }

  protected static void editOwlAction(Owl<?> owl) {
    if (owl.entity() instanceof Controllable) {
      Controllable controlEntity = (Controllable) owl.entity();
      try {
        controlEntity.getController().show();
      } catch (IOException e) {
        Owlook.registerException(1, e);
      }
    }
  }

  protected void editOwlPreviewAction(Owl<?> owl) {
    if (CONFIG.getEditOwlPreview()) {
      if (owl == null) {
        DESIGN.setEditOwlPreview(null);

      } else if (owl.entity() instanceof Controllable) {
        Controllable controlEntity = (Controllable) owl.entity();
        try {
          DESIGN.setEditOwlPreview(controlEntity.getController().getParent());
        } catch (IOException e) {
          Owlook.registerException(1, e);
        }
      } else {
        DESIGN.setEditOwlPreview(null);
      }
    }
  }

  protected static void deleteOwlAction(List<Owl<?>> owls) {
    List<Owl<?>> deleteOwls = new ArrayList<>(owls);
    OwlLoader loader = OwlLoader.INSTANCE;
    BiConsumer<IOException, Owl<?>> exceptionHandler = (e, owl) -> {
      Owlook.registerException(1, e);

      OwlookMessage message = new OwlookMessage(MessageLevel.ERROR,
          "Deletion error " + owl.info().owlName() + ": " + owl.head().getTitle(),
          e.getClass().getSimpleName() + ": " + e.getMessage());
      Owlook.notificate(message);

    };
    if (deleteOwls.size() == 1) {
      try {
        loader.deleteOwl(deleteOwls.get(0));
      } catch (IOException e) {
        exceptionHandler.accept(e, deleteOwls.get(0));
      }

    } else if (deleteOwls.size() > 1) {
      deleteOwls.forEach(owl -> {
        try {
          loader.deleteOwl(owl, DeleteFlag.NO_DEPENDENCIES);
        } catch (IOException e) {
          exceptionHandler.accept(e, owl);
        }
      });
    }
  }
}
