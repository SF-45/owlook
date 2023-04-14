package space.sadfox.owlook.ui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import space.sadfox.owlook.Main;
import space.sadfox.owlook.moduleapi.Module;
import space.sadfox.owlook.moduleapi.Workspace;
import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.utils.ModuleLoader;

public class MainStage extends Controller {
	
	@FXML
    private ListView<Module> moduleList;

    @FXML
    private ListView<Workspace> wsList;

	public MainStage() throws IOException {
		super(Main.class.getResource("fxml/main-scene.fxml"));
		
		moduleList.setCellFactory(param -> {
			return new ListCell<> () {
				@Override
	            public void updateItem(Module module, boolean empty) {
	                super.updateItem(module, empty);
	                if (empty || module == null) {
	                    setText(null);
	                } else {
	                    setText(module.getModuleName() + " " + module.getModuleVersion());
	                }
	            }
			};
		});
		wsList.setCellFactory(param -> {
			ListCell<Workspace> cell = new ListCell<> () {
				@Override
	            public void updateItem(Workspace wp, boolean empty) {
	                super.updateItem(wp, empty);
	                if (empty || wp == null) {
	                    setText(null);
	                } else {
	                    setText(wp.getWorkspaceName());
	                }
	            }
			};
			ContextMenu contextMenu = new ContextMenu();
			MenuItem open = new MenuItem("Open workspace");
			open.setOnAction(event -> {
				cell.getItem().getController().show();
				
			});
			contextMenu.getItems().add(open);
			cell.setContextMenu(contextMenu);
			return cell;
		});
		
		moduleList.setItems(FXCollections.observableList(ModuleLoader.INSTANCE.loadModules()));
		wsList.setItems(FXCollections.observableList(ModuleLoader.INSTANCE.loadWorkspaces()));
		
		
	}

}
