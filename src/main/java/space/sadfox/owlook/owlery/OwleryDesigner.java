package space.sadfox.owlook.owlery;

import static space.sadfox.owlook.owlery.OwlTableView.DATE_COLUMN_NAME;
import static space.sadfox.owlook.owlery.OwlTableView.ID_COLUMN_NAME;
import static space.sadfox.owlook.owlery.OwlTableView.MODULE_COLUMN_NAME;
import static space.sadfox.owlook.owlery.OwlTableView.OWL_NAME_COLUMN_NAME;
import static space.sadfox.owlook.owlery.OwlTableView.TITLE_COLUMN_NAME;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import space.sadfox.owlook.owlery.OwleryConfig.SelectedShowBy;
import space.sadfox.owlook.ui.base.FormDesigner;
import space.sadfox.owlook.ui.base.FormDesigners;

public class OwleryDesigner extends FormDesigner {
  public static class ViewMenu extends Menu {
    final Menu showByMenu;
    final RadioMenuItem showAllMenuItem;
    final RadioMenuItem showByOwlNameMenuItem;
    final RadioMenuItem showByModuleMenuItem;
    final ToggleGroup showByToggleGroup = new ToggleGroup();

    final CheckMenuItem idColumnVisible;
    final CheckMenuItem titleColumnVisible;
    final CheckMenuItem owlNameColumnVisible;
    final CheckMenuItem moduleColumnVisible;
    final CheckMenuItem dateColumnVisible;

    final CheckMenuItem editOwlPreview;
    final RadioMenuItem showImportedMenuItem;

    private ViewMenu() {
      setText("View");
      showByMenu = FormDesigners.addTo(this, new Menu("Show By"));
      showAllMenuItem = FormDesigners.addTo(showByMenu, new RadioMenuItem("Show All"));
      showAllMenuItem.setToggleGroup(showByToggleGroup);

      showByOwlNameMenuItem = FormDesigners.addTo(showByMenu, new RadioMenuItem("Owl Name"));
      showByOwlNameMenuItem.setToggleGroup(showByToggleGroup);

      showByModuleMenuItem = FormDesigners.addTo(showByMenu, new RadioMenuItem("Module"));
      showByModuleMenuItem.setToggleGroup(showByToggleGroup);

      Menu columns = FormDesigners.addTo(this, new Menu("Columns"));
      idColumnVisible = FormDesigners.addTo(columns, new CheckMenuItem(ID_COLUMN_NAME));
      titleColumnVisible = FormDesigners.addTo(columns, new CheckMenuItem(TITLE_COLUMN_NAME));
      owlNameColumnVisible = FormDesigners.addTo(columns, new CheckMenuItem(OWL_NAME_COLUMN_NAME));
      moduleColumnVisible = FormDesigners.addTo(columns, new CheckMenuItem(MODULE_COLUMN_NAME));
      dateColumnVisible = FormDesigners.addTo(columns, new CheckMenuItem(DATE_COLUMN_NAME));

      editOwlPreview = FormDesigners.addTo(this, new CheckMenuItem("Edit Preview"));

      showImportedMenuItem = FormDesigners.addTo(this, new RadioMenuItem("Show imported"));
      showImportedMenuItem.setToggleGroup(showByToggleGroup);
    }
  }

  public static class FileMenu extends Menu {
    final MenuItem importOwls;
    final MenuItem exportOwls;

    private FileMenu() {
      setText("File");
      importOwls = FormDesigners.addTo(this, new MenuItem("Import Owls"));
      exportOwls = FormDesigners.addTo(this, new MenuItem("Export Owls"));
    }
  }

  public static class EditMenu extends Menu {
    final Menu createOwlMenu;

    private EditMenu() {
      setText("Edit");
      createOwlMenu = FormDesigners.addTo(this, new Menu("Create Owl"));

    }
  }

  final VBox root = new VBox();
  final SplitPane body;

  final MenuBar leftMenuBar;
  final MenuBar rightMenuBar;
  final FileMenu fileMenu;
  final EditMenu editMenu;
  final ViewMenu viewMenu;

  private Node currentOwlEditPreview;

  final TextField searchOwlTextField;
  final ListView<String> groupListView;
  final OwlTableView owlTableView;

  OwleryDesigner(OwleryConfig config) {
    root.setPrefWidth(600);

    // MENU_BAR
    HBox menuBarHBox = FormDesigners.addTo(root, new HBox());
    FormDesigners.bindMinWidth(root, menuBarHBox);
    VBox.setMargin(menuBarHBox, new Insets(0d, 0d, 5d, 0d));

    leftMenuBar = FormDesigners.addTo(menuBarHBox, new MenuBar());
    rightMenuBar = FormDesigners.addTo(menuBarHBox, new MenuBar());
    rightMenuBar.setMinWidth(180d);
    FormDesigners.bindMinHeight(rightMenuBar, leftMenuBar);
    leftMenuBar.minWidthProperty()
        .bind(menuBarHBox.widthProperty().subtract(rightMenuBar.widthProperty()));

    fileMenu = FormDesigners.addTo(leftMenuBar, new FileMenu());
    editMenu = FormDesigners.addTo(leftMenuBar, new EditMenu());
    viewMenu = FormDesigners.addTo(leftMenuBar, new ViewMenu());

    viewMenu.showAllMenuItem.selectedProperty().addListener((property, oldValue, newValue) -> {
      if (newValue) {
        config.setSelectedShowBy(SelectedShowBy.ALL);
      }
    });

    viewMenu.showByOwlNameMenuItem.selectedProperty()
        .addListener((property, oldValue, newValue) -> {
          if (newValue) {
            config.setSelectedShowBy(SelectedShowBy.OWL_NAME);
          }
        });

    viewMenu.showByModuleMenuItem.selectedProperty().addListener((property, oldValue, newValue) -> {
      if (newValue) {
        config.setSelectedShowBy(SelectedShowBy.MODULE);
      }
    });

    switch (config.getSelectedShowBy()) {
      case ALL:
        viewMenu.showAllMenuItem.setSelected(true);
        break;
      case OWL_NAME:
        viewMenu.showByOwlNameMenuItem.setSelected(true);
        break;
      case MODULE:
        viewMenu.showByModuleMenuItem.setSelected(true);
        break;
      default:
        break;
    }


    viewMenu.idColumnVisible.setSelected(config.getIdColumnVisible());
    config.idColumnVisibleProperty().bind(viewMenu.idColumnVisible.selectedProperty());

    viewMenu.titleColumnVisible.setSelected(config.getTitleColumnVisible());
    config.titleColumnVisibleProperty().bind(viewMenu.titleColumnVisible.selectedProperty());

    viewMenu.owlNameColumnVisible.setSelected(config.getOwlNameColumnVisible());
    config.owlNameColumnVisibleProperty().bind(viewMenu.owlNameColumnVisible.selectedProperty());

    viewMenu.moduleColumnVisible.setSelected(config.getModuleColumnVisible());
    config.moduleColumnVisibleProperty().bind(viewMenu.moduleColumnVisible.selectedProperty());

    viewMenu.dateColumnVisible.setSelected(config.getDateColumnVisible());
    config.dateColumnVisibleProperty().bind(viewMenu.dateColumnVisible.selectedProperty());

    viewMenu.editOwlPreview.setSelected(config.getEditOwlPreview());
    config.editOwlPreviewProperty().bind(viewMenu.editOwlPreview.selectedProperty());
    config.editOwlPreviewProperty().addListener((property, oldValue, newValue) -> {
      if (newValue == false) {
        setEditOwlPreview(null);
      }
    });

    searchOwlTextField = new TextField();
    searchOwlTextField.setPromptText("Search Owl");
    FormDesigners.addTo(rightMenuBar, new Menu("", searchOwlTextField));

    // BODY
    body = FormDesigners.addTo(root, new SplitPane());
    FormDesigners.bindMinWidth(root, body);
    body.minHeightProperty().bind(root.heightProperty().subtract(menuBarHBox.heightProperty()));
    body.setOrientation(Orientation.HORIZONTAL);
    body.getDividers().addListener((InvalidationListener) change -> {
      Divider group = null;
      Divider preview = null;
      if (body.getDividers().size() == 1) {
        if (!config.getSelectedShowBy().equals(OwleryConfig.SelectedShowBy.ALL)) {
          group = body.getDividers().get(0);
        } else {
          preview = body.getDividers().get(0);
        }
      } else if (body.getDividers().size() > 1) {
        group = body.getDividers().get(0);
        preview = body.getDividers().get(1);
      }


      if (group != null) {
        group.setPosition(config.getGroupDividerPos());
        config.groupDividerPosProperty().bind(group.positionProperty());
      }
      if (preview != null) {
        preview.setPosition(config.getEditPreviewDividerPos());
        config.editPreviewDividerPosProperty().bind(preview.positionProperty());
      }
    });

    groupListView = new ListView<>();

    owlTableView = FormDesigners.addTo(body, new OwlTableView());
    owlTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    owlTableView.ID.visibleProperty().bind(viewMenu.idColumnVisible.selectedProperty());
    owlTableView.TITLE.visibleProperty().bind(viewMenu.titleColumnVisible.selectedProperty());
    owlTableView.OWL_NAME.visibleProperty().bind(viewMenu.owlNameColumnVisible.selectedProperty());
    owlTableView.MODULE_NAME.visibleProperty()
        .bind(viewMenu.moduleColumnVisible.selectedProperty());
    owlTableView.DATE_CREATED.visibleProperty().bind(viewMenu.dateColumnVisible.selectedProperty());
    owlTableView.setMinHeight(TableView.USE_COMPUTED_SIZE);

  }

  @Override
  protected Parent root() {
    return root;
  }

  void setGroupListViewVisible(boolean visible) {
    if (visible && !body.getItems().contains(groupListView)) {
      body.getItems().add(0, groupListView);
    } else if (!visible) {
      body.getItems().remove(groupListView);
    }
  }

  void setEditOwlPreview(Node node) {
    if (currentOwlEditPreview != null) {
      body.getItems().remove(currentOwlEditPreview);
      currentOwlEditPreview = null;
    }
    if (node != null) {
      body.getItems().add(node);
      currentOwlEditPreview = node;
    }
  }

}
