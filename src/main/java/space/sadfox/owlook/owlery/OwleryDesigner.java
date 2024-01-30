package space.sadfox.owlook.owlery;

import static space.sadfox.owlook.owlery.OwleryConfig.DATE_COLUMN_HEAD;
import static space.sadfox.owlook.owlery.OwleryConfig.ID_COLUMN_HEAD;
import static space.sadfox.owlook.owlery.OwleryConfig.MODULE_COLUMN_HEAD;
import static space.sadfox.owlook.owlery.OwleryConfig.OWL_NAME_COLUMN_HEAD;
import static space.sadfox.owlook.owlery.OwleryConfig.TITLE_COLUMN_HEAD;

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

	final VBox root = new VBox();
	final SplitPane body;

	final MenuItem importOwls;
	final MenuItem exportOwls;

	final Menu createOwlMenu;

	final RadioMenuItem showAllMenuItem;
	final RadioMenuItem showByOwlNameMenuItem;
	final RadioMenuItem showByModuleMenuItem;
	final RadioMenuItem showImportedMenuItem;
	final ToggleGroup showByGroup = new ToggleGroup();

	final CheckMenuItem idColumnVisible;
	final CheckMenuItem titleColumnVisible;
	final CheckMenuItem owlNameColumnVisible;
	final CheckMenuItem moduleColumnVisible;
	final CheckMenuItem dateColumnVisible;

	final CheckMenuItem editOwlPreview;
	private Node currentOwlEditPreview;

	final TextField searchOwlTextField;
	final ListView<String> groupListView;
	final OwlTableView owlTableView;

	private final OwleryConfig config = OwleryConfig.instance();

	OwleryDesigner() {
		root.setPrefWidth(600);

		// MENU_BAR
		HBox menuBarHBox = FormDesigners.addTo(root, new HBox());
		FormDesigners.bindMinWidth(root, menuBarHBox);
		VBox.setMargin(menuBarHBox, new Insets(0d, 0d, 5d, 0d));

		MenuBar leftMenuBar = FormDesigners.addTo(menuBarHBox, new MenuBar());
		MenuBar rightMenuBar = FormDesigners.addTo(menuBarHBox, new MenuBar());
		rightMenuBar.setMinWidth(180d);
		FormDesigners.bindMinHeight(rightMenuBar, leftMenuBar);
		leftMenuBar.minWidthProperty().bind(menuBarHBox.widthProperty().subtract(rightMenuBar.widthProperty()));

		Menu fileMenu = FormDesigners.addTo(leftMenuBar, new Menu("File"));

		importOwls = FormDesigners.addTo(fileMenu, new MenuItem("Import Owls"));

		exportOwls = FormDesigners.addTo(fileMenu, new MenuItem("Export Owls"));

		Menu editMenu = FormDesigners.addTo(leftMenuBar, new Menu("Edit"));

		createOwlMenu = FormDesigners.addTo(editMenu, new Menu("Create Owl"));

		Menu viewMenu = FormDesigners.addTo(leftMenuBar, new Menu("View"));

		Menu showBy = FormDesigners.addTo(viewMenu, new Menu("Show By"));

		showAllMenuItem = FormDesigners.addTo(showBy, new RadioMenuItem("Show All"));
		showAllMenuItem.setToggleGroup(showByGroup);
		showAllMenuItem.selectedProperty().addListener((property, oldValue, newValue) -> {
			if (newValue) {
				config.setSelectedShowBy(SelectedShowBy.ALL);
			}
		});

		showByOwlNameMenuItem = FormDesigners.addTo(showBy, new RadioMenuItem("Owl Name"));
		showByOwlNameMenuItem.setToggleGroup(showByGroup);
		showByOwlNameMenuItem.selectedProperty().addListener((property, oldValue, newValue) -> {
			if (newValue) {
				config.setSelectedShowBy(SelectedShowBy.OWL_NAME);
			}
		});

		showByModuleMenuItem = FormDesigners.addTo(showBy, new RadioMenuItem("Module"));
		showByModuleMenuItem.setToggleGroup(showByGroup);
		showByModuleMenuItem.selectedProperty().addListener((property, oldValue, newValue) -> {
			if (newValue) {
				config.setSelectedShowBy(SelectedShowBy.MODULE);
			}
		});

		switch (config.getSelectedShowBy()) {
		case ALL:
			showAllMenuItem.setSelected(true);
			break;
		case OWL_NAME:
			showByOwlNameMenuItem.setSelected(true);
			break;
		case MODULE:
			showByModuleMenuItem.setSelected(true);
			break;
		default:
			break;
		}

		Menu columns = FormDesigners.addTo(viewMenu, new Menu("Columns"));

		idColumnVisible = FormDesigners.addTo(columns, new CheckMenuItem(ID_COLUMN_HEAD));
		idColumnVisible.setSelected(config.getIdColumnVisible());
		config.idColumnVisibleProperty().bind(idColumnVisible.selectedProperty());

		titleColumnVisible = FormDesigners.addTo(columns, new CheckMenuItem(TITLE_COLUMN_HEAD));
		titleColumnVisible.setSelected(config.getTitleColumnVisible());
		config.titleColumnVisibleProperty().bind(titleColumnVisible.selectedProperty());

		owlNameColumnVisible = FormDesigners.addTo(columns, new CheckMenuItem(OWL_NAME_COLUMN_HEAD));
		owlNameColumnVisible.setSelected(config.getOwlNameColumnVisible());
		config.owlNameColumnVisibleProperty().bind(owlNameColumnVisible.selectedProperty());

		moduleColumnVisible = FormDesigners.addTo(columns, new CheckMenuItem(MODULE_COLUMN_HEAD));
		moduleColumnVisible.setSelected(config.getModuleColumnVisible());
		config.moduleColumnVisibleProperty().bind(moduleColumnVisible.selectedProperty());

		dateColumnVisible = FormDesigners.addTo(columns, new CheckMenuItem(DATE_COLUMN_HEAD));
		dateColumnVisible.setSelected(config.getDateColumnVisible());
		config.dateColumnVisibleProperty().bind(dateColumnVisible.selectedProperty());

		showImportedMenuItem = FormDesigners.addTo(viewMenu, new RadioMenuItem("Show imported"));
		showImportedMenuItem.setToggleGroup(showByGroup);

		editOwlPreview = FormDesigners.addTo(viewMenu, new CheckMenuItem("Edit Preview"));
		editOwlPreview.setSelected(config.getEditOwlPreview());
		config.editOwlPreviewProperty().bind(editOwlPreview.selectedProperty());
		config.editOwlPreviewProperty().addListener((property, oldValue, newValue) -> {
			if (newValue == false) {
				setEditOwlPreview(null);
			}
		});

		searchOwlTextField = new TextField();
		searchOwlTextField.setPromptText("Search Owl");
		Menu searchMenu = FormDesigners.addTo(rightMenuBar, new Menu("", searchOwlTextField));

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
		owlTableView.ID.visibleProperty().bind(idColumnVisible.selectedProperty());
		owlTableView.TITLE.visibleProperty().bind(titleColumnVisible.selectedProperty());
		owlTableView.OWL_NAME.visibleProperty().bind(owlNameColumnVisible.selectedProperty());
		owlTableView.MODULE_NAME.visibleProperty().bind(moduleColumnVisible.selectedProperty());
		owlTableView.DATE_CREATED.visibleProperty().bind(dateColumnVisible.selectedProperty());
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
