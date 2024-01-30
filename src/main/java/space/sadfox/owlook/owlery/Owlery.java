package space.sadfox.owlook.owlery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import space.sadfox.owlook.base.moduleapi.OwlookModuleInfo;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.owlery.OwleryConfig.SelectedShowBy;
import space.sadfox.owlook.ui.base.Controllable;
import space.sadfox.owlook.ui.base.DesignController;
import space.sadfox.owlook.ui.base.FormDesigners;
import space.sadfox.owlook.utils.Logger;

public class Owlery extends DesignController<OwleryDesigner> {

  private OwleryConfig config = OwleryConfig.instance();
  private final ObservableList<Owl<?>> visibleOwls = FXCollections.observableArrayList();
  private final ObservableList<Owl<?>> owls = FXCollections.observableArrayList();
  private String selectedGroup = "";

  public Owlery() {
    super(new OwleryDesigner());

    stageTitle.set("Owlery");

    owls.addListener((InvalidationListener) prop -> {
      DESIGN.searchOwlTextField.setText("");
      filter("");
    });

    DESIGN.createOwlMenu.getItems().addAll(createOwlMenuItems());
    DESIGN.groupListView.getSelectionModel().selectedItemProperty()
        .addListener((property, oldValue, newValue) -> {
          if (newValue == null) {
            owls.clear();
          } else {
            selectGroup(newValue);
          }
        });

    config.selectedShowByProperty().addListener((property, oldValue, newValue) -> {
      showBy(newValue);
    });
    showBy(config.getSelectedShowBy());

    DESIGN.searchOwlTextField.textProperty().addListener((property, oldValue, newValue) -> {
      filter(newValue);

    });

    DESIGN.owlTableView.setItems(visibleOwls);

    OwlLoader.INSTANCE.addCreateOwlListener(newOwl -> {
      updateSelectGroup();
    });
    OwlLoader.INSTANCE.addDeleteOwlListener(delOwl -> {
      updateSelectGroup();
    });

    ContextMenu tableContextMenu = FormDesigners.addTo(DESIGN.owlTableView, new ContextMenu());
    MenuItem editOwl = FormDesigners.addTo(tableContextMenu, new MenuItem("Edit Owl"));
    editOwl.setOnAction(event -> {
      TableViewSelectionModel<Owl<?>> select = DESIGN.owlTableView.getSelectionModel();
      editOwlAction(select.getSelectedItem());

    });

    MenuItem deleteOwl = FormDesigners.addTo(tableContextMenu, new MenuItem("Delete Owl"));
    deleteOwl.setOnAction(event -> {
      deleteOwlAction(DESIGN.owlTableView.getSelectionModel().getSelectedItems());

    });

    Menu createOwl = FormDesigners.addTo(tableContextMenu, new Menu("Create Owl"));
    createOwl.getItems().addAll(createOwlMenuItems());

    DESIGN.owlTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
      if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
        editOwlAction(DESIGN.owlTableView.getSelectionModel().getSelectedItem());
      }
    });
    DESIGN.owlTableView.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
      switch (keyEvent.getCode()) {
        case DELETE:
          deleteOwlAction(DESIGN.owlTableView.getSelectionModel().getSelectedItems());
          break;

        default:
          break;
      }
    });
    DESIGN.owlTableView.setOnContextMenuRequested(contextMenuEvent -> {
      TableViewSelectionModel<Owl<?>> select = DESIGN.owlTableView.getSelectionModel();
      if (select.getSelectedItems().size() == 0) {
        tableContextMenu.hide();
      } else if (select.getSelectedItems().size() == 1) {
        editOwl.setDisable(false);
      } else if (select.getSelectedItems().size() > 1) {
        editOwl.setDisable(true);
      }
    });

    DESIGN.owlTableView.getSelectionModel().selectedItemProperty()
        .addListener((property, oldValue, newValue) -> {
          ObservableList<Owl<?>> selectedItems =
              DESIGN.owlTableView.getSelectionModel().getSelectedItems();
          if (selectedItems.size() == 1) {
            editOwlPreviewAction(selectedItems.get(0));
          } else {
            editOwlPreviewAction(null);
          }
        });

  }

  private void selectGroup(String group) {
    owls.clear();
    selectedGroup = group;
    switch (config.getSelectedShowBy()) {
      case OWL_NAME:
        owls.addAll(OwlLoader.INSTANCE.getOwls().stream()
            .filter(owl -> owl.info().owlName().equals(group)).collect(Collectors.toList()));
        break;
      case MODULE:
        OwlLoader.INSTANCE.getOwls().forEach(owl -> {
          List<String> moduleNames =
              ModuleLoader.INSTANCE.getLoadedModules(owl.info().createdModule()).stream()
                  .map(OwlookModuleInfo::name).collect(Collectors.toList());
          if (moduleNames.contains(group)) {
            owls.add(owl);
          }
        });

        owls.addAll(OwlLoader.INSTANCE.getOwls().stream()
            .filter(owl -> owl.info().owlName().equals(group)).collect(Collectors.toList()));
        break;
      case ALL:
        owls.addAll(OwlLoader.INSTANCE.getOwls());
      default:
        break;
    }
  }

  private void updateSelectGroup() {
    selectGroup(selectedGroup);
  }

  private void filter(String filter) {
    visibleOwls.clear();
    if (filter.equals("")) {
      visibleOwls.addAll(owls);
    } else {
      visibleOwls.addAll(
          owls.filtered(owl -> owl.head().getTitle().toLowerCase().contains(filter.toLowerCase())));
    }
  }

  private void showBy(SelectedShowBy selectedShowBy) {
    owls.clear();
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
        selectGroup("");
        break;

      default:
        break;
    }
  }

  private List<MenuItem> createOwlMenuItems() {
    List<MenuItem> createMenuItems = new ArrayList<>();
    ModuleLoader.INSTANCE.loadOwlEntities().forEach(owlEntity -> {
      MenuItem createOwlMenuItem = new MenuItem(owlEntity.getEntityName());
      createOwlMenuItem.setOnAction(event -> {
        try {
          Owl<?> newOwl = OwlLoader.INSTANCE.createOwl(owlEntity.getClass());
        } catch (Exception e) {
          Logger.registerException(0, e);
        }
      });
      createMenuItems.add(createOwlMenuItem);
    });
    return createMenuItems;

  }

  private void editOwlAction(Owl<?> owl) {
    if (owl.entity() instanceof Controllable) {
      Controllable controlEntity = (Controllable) owl.entity();
      try {
        controlEntity.getController().show();
      } catch (IOException e) {
        Logger.registerException(1, e);
      }
    }
  }

  private void editOwlPreviewAction(Owl<?> owl) {
    if (config.getEditOwlPreview()) {
      if (owl == null) {
        DESIGN.setEditOwlPreview(null);

      } else if (owl.entity() instanceof Controllable) {
        Controllable controlEntity = (Controllable) owl.entity();
        try {
          DESIGN.setEditOwlPreview(controlEntity.getController().getParent());
        } catch (IOException e) {
          Logger.registerException(1, e);
        }
      } else {
        DESIGN.setEditOwlPreview(null);
      }
    }
  }

  private void deleteOwlAction(List<Owl<?>> owls) {
    List<Owl<?>> deleteOwls = new ArrayList<>(owls);
    deleteOwls.forEach(owl -> {
      try {
        OwlLoader.INSTANCE.deleteOwl(owl);
      } catch (Exception e) {
        Logger.registerException(1, e); // TODO: Окно с подтверждением и уведомление об ошибке
      }
    });
  }

}
