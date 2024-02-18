package space.sadfox.owlook.owlery;

import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import space.sadfox.owlook.base.moduleapi.OwlookModuleInfo;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.ui.base.FormDesigners;

class OwlTableView extends TableView<Owl<?>> {
  public static final String ID_COLUMN_NAME = "ID";
  public static final String TITLE_COLUMN_NAME = "Title";
  public static final String OWL_NAME_COLUMN_NAME = "Owl Name";
  public static final String MODULE_COLUMN_NAME = "Module";
  public static final String DATE_COLUMN_NAME = "Date";

  public final TableColumn<Owl<?>, String> ID;
  public final TableColumn<Owl<?>, String> TITLE;
  public final TableColumn<Owl<?>, String> OWL_NAME;
  public final TableColumn<Owl<?>, String> MODULE_NAME;
  public final TableColumn<Owl<?>, String> DATE_CREATED;

  private Consumer<TableRow<Owl<?>>> doubleClickEvent;

  public final ContextMenu ROW_CONTEXT_MENU = new ContextMenu();

  public OwlTableView() {
    ID = FormDesigners.addTo(this, new TableColumn<>(ID_COLUMN_NAME));
    ID.setCellValueFactory(cellData -> {
      return new SimpleStringProperty(cellData.getValue().info().id().toString());
    });

    TITLE = FormDesigners.addTo(this, new TableColumn<>(TITLE_COLUMN_NAME));
    TITLE.setCellValueFactory(cellData -> {
      return cellData.getValue().head().titleProperty();
    });

    OWL_NAME = FormDesigners.addTo(this, new TableColumn<>(OWL_NAME_COLUMN_NAME));
    OWL_NAME.setCellValueFactory(cellData -> {
      return new SimpleStringProperty(cellData.getValue().info().owlName());
    });

    MODULE_NAME = FormDesigners.addTo(this, new TableColumn<>(MODULE_COLUMN_NAME));
    MODULE_NAME.setCellValueFactory(cellData -> {
      Optional<OwlookModuleInfo> moduleName =
          ModuleLoader.INSTANCE.getLoadedModules(cellData.getValue().info().createdModule());
      if (moduleName.isPresent()) {
        return new SimpleStringProperty(moduleName.get().name());
      } else {
        return new SimpleStringProperty("Not Found");
      }
    });

    DATE_CREATED = FormDesigners.addTo(this, new TableColumn<>(DATE_COLUMN_NAME));
    DATE_CREATED.setCellValueFactory(cellData -> {
      return new SimpleStringProperty(
          new Date(cellData.getValue().info().createdTime()).toString());
    });

    this.setRowFactory(callback -> {
      TableRow<Owl<?>> row = new TableRow<>();
      row.setContextMenu(ROW_CONTEXT_MENU);
      row.setOnContextMenuRequested(event -> {
        if (row.isEmpty()) {
          ROW_CONTEXT_MENU.hide();
        }
      });

      row.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
        if (row.isEmpty() || doubleClickEvent == null)
          return;
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
          doubleClickEvent.accept(row);
        }
      });
      return row;
    });
  }

  public void setOnDoubleClickEvent(Consumer<TableRow<Owl<?>>> consumer) {
    doubleClickEvent = consumer;
  }


}
