package space.sadfox.owlook.owlery;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import space.sadfox.owlook.base.moduleapi.OwlookModuleInfo;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.owlery.OwleryConfig.SelectedShowBy;

public class Owlery extends OwleryCRUD {

  private final StringProperty selectedGroup = new SimpleStringProperty("");

  public Owlery() {
    super(OwleryConfig.instance());

    setListenFiltersChange(true);
    setListenSearchChange(true);
    setListenAddRemoveOwls(true);

    DESIGN.owlTableView.setOnDoubleClickEvent(row -> editOwlAction(row.getItem()));

    stageTitle.set("Owlery");

    addOwlsFilter(owl -> {
      switch (CONFIG.getSelectedShowBy()) {
        case OWL_NAME:
          return owl.entity().getEntityName().equals(selectedGroup.get());
        case MODULE:
          Optional<OwlookModuleInfo> moduleInfo =
              ModuleLoader.INSTANCE.getLoadedModules(owl.info().createdModule());
          if (moduleInfo.isPresent()) {
            return moduleInfo.get().name().equals(selectedGroup.get());
          } else {
            return true;
          }
        default:
          return true;
      }
    });
    selectedGroup.addListener((property, oldValue, newValue) -> {
      updateVisibleOwls();
    });

    DESIGN.groupListView.getSelectionModel().selectedItemProperty()
        .addListener((property, oldValue, newValue) -> {
          if (newValue != null) {
            selectedGroup.set(newValue);
          }
        });

    CONFIG.selectedShowByProperty().addListener((property, oldValue, newValue) -> {
      showBy(newValue);
    });
    showBy(CONFIG.getSelectedShowBy());
  }

  private void showBy(SelectedShowBy selectedShowBy) {
    switch (selectedShowBy) {
      case OWL_NAME:
        DESIGN.setGroupListViewVisible(true);
        DESIGN.groupListView.getItems().clear();
        List<String> owlNames = ModuleLoader.INSTANCE.loadOwlEntities().stream()
            .map(owlEntity -> owlEntity.getEntityName()).collect(Collectors.toList());
        DESIGN.groupListView.getItems().addAll(owlNames);
        break;
      case MODULE:
        DESIGN.setGroupListViewVisible(true);
        DESIGN.groupListView.getItems().clear();

        Set<String> moduleNames = new HashSet<>();

        ModuleLoader.INSTANCE.loadOwlEntities().forEach(owlEntity -> {
          Optional<OwlookModuleInfo> moduleInfo =
              ModuleLoader.INSTANCE.getLoadedModules(owlEntity.getClass().getModule().getName());
          if (moduleInfo.isPresent()) {
            moduleNames.add(moduleInfo.get().name());
          }
        });

        DESIGN.groupListView.getItems().addAll(moduleNames);
        break;
      case ALL:
        DESIGN.setGroupListViewVisible(false);
        DESIGN.groupListView.getItems().clear();
        break;

      default:
        break;
    }
    updateVisibleOwls();
  }
}
