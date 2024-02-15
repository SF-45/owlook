package space.sadfox.owlook.ui;

import java.util.Optional;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import space.sadfox.owlook.api.Workspace;
import space.sadfox.owlook.base.moduleapi.OwlookModuleInfo;
import space.sadfox.owlook.moduleloader.ModuleLoader;

public class WSItemList extends ListView<Workspace> {

  private class WorkspaceListCell extends ListCell<Workspace> {

    public WorkspaceListCell() {
      addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
        switch (mouseEvent.getButton()) {
          case PRIMARY:
            if (mouseEvent.getClickCount() == 2) {
              Workspace ws = this.getItem();
              if (ws != null) {
                ws.getController().show();
              }
            }
            break;

          default:
            break;
        }
      });

    }

    @Override
    protected void updateItem(Workspace ws, boolean b) {
      super.updateItem(ws, b);
      if (b || ws == null) {
        setText(null);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
      } else {
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(new WorkspaceListItem(ws));
      }

    }
  }

  private class WorkspaceListItem extends GridPane {

    public WorkspaceListItem(Workspace ws) {
      ColumnConstraints column = new ColumnConstraints();
      column.prefWidthProperty().bind(thisListView.widthProperty().subtract(20d));
      column.setHgrow(Priority.NEVER);
      getColumnConstraints().add(column);

      Optional<OwlookModuleInfo> module =
          ModuleLoader.INSTANCE.getLoadedModules(ws.getClass().getModule().getName());

      Label name = new Label(ws.getWorkspaceName());
      name.setFont(new Font("System Bold", 20d));
      this.add(name, 0, 1);

      Button config = new Button("⚙");
      GridPane.setHalignment(config, HPos.RIGHT);
      this.add(config, 0, 1);

      Label description = new Label(ws.getWorkspaceDescriprion());
      description.setWrapText(true);
      GridPane.setMargin(description, new Insets(20d));
      this.add(description, 0, 2);

      Label modName = new Label();
      if (module.isPresent()) {
        modName.setText("Provide by [" + module.get().name() + "]");
      } else {
        modName.setText("Provide by [Not found]");
      }
      this.add(modName, 0, 3);

      Label version = new Label();
      if (module.isPresent()) {
        version.setText(module.get().version().toString());
      }
      GridPane.setHalignment(version, HPos.RIGHT);
      this.add(version, 0, 3);
    }

  }

  private final ListView<Workspace> thisListView;

  public WSItemList() {
    thisListView = this;
    setCellFactory(call -> new WorkspaceListCell());
  }


}
