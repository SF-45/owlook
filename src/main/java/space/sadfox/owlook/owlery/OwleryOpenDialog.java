package space.sadfox.owlook.owlery;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyEvent;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;

public class OwleryOpenDialog<T extends OwlEntity> extends OwleryCRUD {

  private final T targetOwlEntity;
  private final List<Owl<T>> openedOwls = new ArrayList<>();
  private boolean opened = false;

  private Predicate<Owl<?>> alredyOpenedOwlsPredicate;
  private ObservableList<Owl<T>> alredyOpenedOwls;
  private final InvalidationListener alredyOpenedOwlsListener;

  public OwleryOpenDialog(Class<T> target) throws ReflectiveOperationException {
    super(getOpenConfig());

    setListenSearchChange(true);
    setListenFiltersChange(true);
    setListenAddRemoveOwls(true);

    targetOwlEntity = target.getConstructor().newInstance();

    alredyOpenedOwlsListener = prop -> updateVisibleOwls();

    stageTitle.set("Open Owl [" + targetOwlEntity.getEntityName() + "]");
    DESIGN.fileMenu.setVisible(false);
    DESIGN.editMenu.createOwlMenu.setVisible(false);
    DESIGN.viewMenu.showByMenu.setVisible(false);
    DESIGN.setGroupListViewVisible(false);

    DESIGN.owlTableView.setOnDoubleClickEvent(row -> openAction());

    DESIGN.owlTableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
      switch (keyEvent.getCode()) {
        case ENTER:
          openAction();
          break;

        default:
          break;
      }
    });

    MenuItem openMenuItem = new MenuItem("Open");
    DESIGN.owlTableView.ROW_CONTEXT_MENU.getItems().add(0, openMenuItem);
    openMenuItem.setOnAction(event -> openAction());

    MenuItem createOwlMenu = new MenuItem("Create " + targetOwlEntity.getEntityName());
    DESIGN.editMenu.getItems().add(createOwlMenu);
    createOwlMenu.setOnAction(event -> OwleryActions.createOwlAction(target));

    addOwlsFilter(owl -> {
      return target.isInstance(owl.entity());
    });

    updateVisibleOwls();
  }

  public void setAlredyOpenedOwls(ObservableList<Owl<T>> owls) {
    if (alredyOpenedOwlsPredicate != null) {
      removeOwlsFilter(alredyOpenedOwlsPredicate);
    }

    if (alredyOpenedOwls != null) {
      alredyOpenedOwls.removeListener(alredyOpenedOwlsListener);
    }

    alredyOpenedOwls = owls;

    if (alredyOpenedOwls != null) {
      alredyOpenedOwlsPredicate = owl -> {
        for (Owl<T> openedOwl : alredyOpenedOwls) {
          boolean checkContain = owl.info().id().equals(openedOwl.info().id());
          if (checkContain) {
            return false;
          }
        }
        return true;
      };
      addOwlsFilter(alredyOpenedOwlsPredicate);
      alredyOpenedOwls.addListener(alredyOpenedOwlsListener);
    }
  }

  public void setAlredyOpenedOwls(Owl<T> owl) {
    ObservableList<Owl<T>> owls = FXCollections.observableArrayList();
    owls.add(owl);
    setAlredyOpenedOwls(owls);
  }


  private static OwleryConfig getOpenConfig() {
    OwleryConfig c = new OwleryConfig();
    c.setEditOwlPreview(false);
    c.setIdColumnVisible(false);
    c.setOwlNameColumnVisible(true);
    c.setModuleColumnVisible(true);
    c.setDateColumnVisible(true);
    return c;
  }

  public void setSelectionModel(SelectionMode selectionMode) {
    DESIGN.owlTableView.getSelectionModel().setSelectionMode(selectionMode);
  }

  public boolean isOpened() {
    return opened;
  }

  public List<Owl<T>> getOpenedOwls() {
    return openedOwls;
  }

  @SuppressWarnings("unchecked")
  private void openAction() {
    var sm = DESIGN.owlTableView.getSelectionModel();
    if (sm.isEmpty())
      return;
    openedOwls.clear();

    sm.getSelectedItems().forEach(owl -> openedOwls.add((Owl<T>) owl));
    opened = true;
    this.getStage().close();

  }
}
