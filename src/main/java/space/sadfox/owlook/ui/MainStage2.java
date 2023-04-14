package space.sadfox.owlook.ui;

import java.io.IOException;
import java.util.List;

import jakarta.xml.bind.JAXBException;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import space.sadfox.owlook.Main;
import space.sadfox.owlook.jaxb.EntityLoader;
import space.sadfox.owlook.jaxb.JAXBEntity;
import space.sadfox.owlook.moduleapi.WorkspaceAPI;
import space.sadfox.owlook.moduleapi.WorkspaceUI;
import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.utils.ErrorLogger;
import space.sadfox.owlook.moduleapi.ModuleBac;

public class MainStage2 extends Controller {

	@FXML
	private BorderPane leftToolPane;

	@FXML
	private BorderPane rightToolPane;

	@FXML
	private MenuItem openIndex;

	@FXML
	private ProgressIndicator saveProgress;

	@FXML
	private Button searchButton;

	@FXML
	private TextField searchField;

	@FXML
	private MenuItem settings;

	@FXML
	private TabPane tableTabPane;

	@FXML
	private Button testButton;

	@FXML
	private Menu workspaceMenu;

	public MainStage2(List<ModuleBac> moduleBacs) throws JAXBException, IOException {
		super(Main.class.getResource("fxml/main-scene2.fxml"));
//		FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/main-scene2.fxml"));
//		loader.setController(this);
//		root = loader.load();
		EntityLoader entityLoader = new EntityLoader();
		for (ModuleBac moduleBac : moduleBacs) {
			List<? extends JAXBEntity> entities = entityLoader.loadAllEntities(moduleBac.getEntityClass());

			if (entities.size() == 0)
				continue;
			if (moduleBac instanceof WorkspaceAPI) {
				WorkspaceAPI workspaceAPI = (WorkspaceAPI) moduleBac;
				Menu menu = new Menu(moduleBac.getName());
				workspaceMenu.getItems().add(menu);

				entities.forEach(entity -> {
					MenuItem wsMenu = new MenuItem(entity.getTitle());
					wsMenu.setOnAction(event -> {
						try {
							WorkspaceUI workspaceUI = workspaceAPI.getWorkspaceUI(entity);
							Tab tab = new Tab(entity.getTitle());
							tab.setContent(workspaceUI.getRootNode());
							tab.setOnSelectionChanged(tabEvent -> {
								if (tab.isSelected()) {
									leftToolPane.setCenter(workspaceUI.getLeftToolsNode());
									rightToolPane.setCenter(workspaceUI.getRightToolsNode());
								}
							});
							tableTabPane.getTabs().add(tab);
							tableTabPane.getSelectionModel().select(tab);
						} catch (JAXBException e) {
							ErrorLogger.registerException(e);
						}
					});
					menu.getItems().add(wsMenu);
				});

			}
		}
		initialize();
	}

//	public void show() {
//		getStage().show();
//	}
//
//	public Parent getParent() {
//		return root;
//	}
//
//	public Stage getStage() {
//		if (rootStage == null) {
//			rootStage = StageFactory.INSTANCE.createStage();
//			rootStage.setScene(new Scene(root));
//		}
//		return rootStage;
//	}

	private void initialize() {
		testButton.setOnAction(event -> {
			System.out.println("Test button");
		});
		tableTabPane.getTabs().addListener((InvalidationListener) listner -> {
			if (tableTabPane.getTabs().size() == 0) {
				leftToolPane.setCenter(null);
				rightToolPane.setCenter(null);
			}
		});
	}

}
