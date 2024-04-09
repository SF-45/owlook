package space.sadfox.owlook.owlery;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.KeyEvent;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.ui.base.FormDesigners;

public abstract class OwleryCRUD extends OwleryBase {

  protected OwleryCRUD(OwleryConfig config) {
    super(config);
    DESIGN.editMenu.createOwlMenu.getItems().addAll(generateCreateOwlMenuItems());

    ContextMenu rowContextMenu = DESIGN.owlTableView.ROW_CONTEXT_MENU;
    MenuItem editOwl = FormDesigners.addTo(rowContextMenu, new MenuItem("Edit Owl"));
    editOwl.setOnAction(event -> {
      TableViewSelectionModel<Owl<?>> select = DESIGN.owlTableView.getSelectionModel();
      editOwlAction(select.getSelectedItem());
    });

    MenuItem duplicateOwl = FormDesigners.addTo(rowContextMenu, new MenuItem("Duplicate Owl"));
    duplicateOwl.setOnAction(event -> {
      TableViewSelectionModel<Owl<?>> select = DESIGN.owlTableView.getSelectionModel();
      duplicateOwlAction(select.getSelectedItem());
    });

    MenuItem deleteOwl = FormDesigners.addTo(rowContextMenu, new MenuItem("Delete Owl"));
    deleteOwl.setOnAction(event -> {
      deleteOwlAction(DESIGN.owlTableView.getSelectionModel().getSelectedItems());

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

  private static List<MenuItem> generateCreateOwlMenuItems() {
    List<MenuItem> createMenuItems = new ArrayList<>();
    ModuleLoader.INSTANCE.loadOwlEntities().forEach(owlEntity -> {
      MenuItem createOwlMenuItem = new MenuItem(owlEntity.getEntityName());
      createOwlMenuItem.setOnAction(event -> {
        createOwlAction(owlEntity.getClass());
      });
      createMenuItems.add(createOwlMenuItem);
    });
    return createMenuItems;
  }
}
