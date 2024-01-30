package space.sadfox.owlook.ui.base;

import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class FormDesigners {
	public static <T extends Node> T addTo(Pane pane, T node) {
		pane.getChildren().add(node);
		return node;
	}
	
	public static <T extends Node> T addTo(ButtonBar buttonBar, T node) {
		buttonBar.getButtons().add(node);
		return node;
	}
	
	public static <T extends Menu> T addTo(MenuBar menuBar, T menu) {
		menuBar.getMenus().add(menu);
		return menu;
	}
	
	public static <T extends MenuItem> T addTo(Menu menu, T menuItem) {
		menu.getItems().add(menuItem);
		return menuItem;
	}
	
	public static <T extends Node> T addTo(SplitPane splitPane, T node) {
		splitPane.getItems().add(node);
		return node;
	}
	
	public static <S, T> TableColumn<S, T> addTo(TableView<S> tableView, TableColumn<S, T> tableColumn) {
		tableView.getColumns().add(tableColumn);
		return tableColumn;
	}
	
	public static void bindMinHeight(Region parent, Region ... childrens) {
		for (Region children : childrens) {
			children.minHeightProperty().bind(parent.heightProperty());
		}
	}
	
	public static void bindMinWidth(Region parent, Region ... childrens) {
		for (Region children : childrens) {
			children.minWidthProperty().bind(parent.widthProperty());
		}
	}
	
	public static <T extends MenuItem> T addTo(ContextMenu contextMenu, T menuItem) {
		contextMenu.getItems().add(menuItem);
		return menuItem;
	}
	
	public static ContextMenu addTo(Control control, ContextMenu contextMenu) {
		control.setContextMenu(contextMenu);
		return contextMenu;
	}


}
