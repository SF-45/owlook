package space.sadfox.owlook.ui;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import space.sadfox.owlook.Main;
import space.sadfox.owlook.component.Workspace;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.owlery.OwlLoader;
import space.sadfox.owlook.owlery.Owlery;
import space.sadfox.owlook.ui.base.FXMLController;

public class MainStage extends FXMLController {

	@FXML
	private ListView<Workspace> wsList;

	public MainStage() throws IOException {
		super(Main.class.getResource("fxml/main-scene.fxml"));
		stageTitle.set("Owlook");
		
		OwlLoader.INSTANCE.boot();

		wsList.setCellFactory(param -> {
			ListCell<Workspace> cell = new ListCell<>() {
				@Override
				public void updateItem(Workspace wp, boolean empty) {
					super.updateItem(wp, empty);
					if (empty || wp == null) {
						setText(null);
					} else {
						setText(wp.getComponentName());
					}
				}
			};
			ContextMenu contextMenu = new ContextMenu();
			MenuItem open = new MenuItem("Open workspace");
			open.setOnAction(event -> {
				cell.getItem().getController().show();

			});
			contextMenu.getItems().add(open);

			// TODO: Удалить
			MenuItem openEntityManager = new MenuItem("Open Entity Manager");
			openEntityManager.setOnAction(event -> {
				new Owlery().show();
			});
			contextMenu.getItems().add(openEntityManager);

			cell.setContextMenu(contextMenu);
			return cell;
		});

		wsList.setItems(FXCollections.observableList(ModuleLoader.INSTANCE.loadWorkspaces()));
		new Owlery().show();

	}

}
