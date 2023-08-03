package space.sadfox.owlook.ui.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.xml.bind.JAXBException;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import space.sadfox.owlook.ResourceTarget;
import space.sadfox.owlook.base.jaxb.JAXBEntity;
import space.sadfox.owlook.base.jaxb.JAXBEntityValidateException;
import space.sadfox.owlook.base.moduleapi.ModuleHasNoProvideEntities;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.ui.base.Controllable;
import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.utils.EntityLoader;
import space.sadfox.owlook.utils.Nullable;
import space.sadfox.owlook.utils.OwlLogger;

public class EntityManager extends Controller {

	@FXML
	private ButtonBar buttonBar;

	@FXML
	private TableView<Class<? extends JAXBEntity>> dirTable;

	@FXML
	private TableView<JAXBEntity> entityTable;

	@FXML
	private TextArea previewArea;

	public EntityManager() throws IOException {
		super(ResourceTarget.class.getResource("fxml/entity-manager.fxml"));

		init();
		initEntityManagerButtons();

		ModuleLoader.INSTANCE.loadModules().forEach(module -> {
			try {
				dirTable.getItems().addAll(module.getJaxbEntities());
			} catch (ModuleHasNoProvideEntities e) {
			}
		});
	}

	public EntityManager(Class<? extends JAXBEntity> targetClass) throws IOException {
		super(ResourceTarget.class.getResource("fxml/entity-manager.fxml"));

		init();
		
		dirTable.setVisible(false);

		dirTable.getItems().add(targetClass);

	}

	private void init() {
		stageTitle.set("Entity Manager");
		EntityLoader.INSTANCE.addCreateChangeListener(entity -> {
			if (dirTable.getSelectionModel().isEmpty())
				return;
			if (dirTable.getSelectionModel().getSelectedItem().equals(entity.getClass())) {
				entityTable.getItems().add(entity);
			}
		});
		EntityLoader.INSTANCE.addDeleteChangeListener(entity -> {
			if (dirTable.getSelectionModel().isEmpty())
				return;
			if (dirTable.getSelectionModel().getSelectedItem().equals(entity.getClass())) {
				entityTable.getItems().remove(entity);
			}
		});
		
		initEntityTable();
		initDirTable();
		initKeyBind();
	}

	private void initEntityTable() {
		TableColumn<JAXBEntity, String> id = new TableColumn<>("ID");
		entityTable.getColumns().add(id);
		id.setCellValueFactory(callback -> new SimpleStringProperty(callback.getValue().getFileName()));

		TableColumn<JAXBEntity, String> title = new TableColumn<>("Title");
		entityTable.getColumns().add(title);
		title.setCellValueFactory(new PropertyValueFactory<>("title"));

		entityTable.getSelectionModel().selectedItemProperty().addListener((property, oldValue, newValue) -> {
			if (newValue == null || newValue.equals(oldValue))
				return;

			previewArea.setText(newValue.toString());
		});

		// ====================================================================================================
		// Context Menu
		// ====================================================================================================
		ContextMenu contextMenu = new ContextMenu();
		entityTable.setContextMenu(contextMenu);

		var selectionModel = entityTable.getSelectionModel();
		selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

		MenuItem create = new MenuItem("Create");
		create.setOnAction(event -> {
			if (dirTable.getSelectionModel().isEmpty())
				return;
			createEntityAction(dirTable.getSelectionModel().getSelectedItem());
		});

		MenuItem edit = new MenuItem("Edit");
		edit.setOnAction(event -> {
			editEntityAction(selectionModel.getSelectedItem());
		});

		MenuItem duplicate = new MenuItem("Duplicate");
		duplicate.setOnAction(event -> {
			duplicateEntityAction(selectionModel.getSelectedItems());
		});

		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(event -> {
			deleteEntityAction(selectionModel.getSelectedItems());
		});

		contextMenu.getItems().addAll(create, edit, duplicate, delete);

		contextMenu.setOnShowing(event -> {
			boolean visible = !selectionModel.isEmpty();
			edit.setVisible(visible);
			duplicate.setVisible(visible);
			delete.setVisible(visible);
		});

	}

	private void initDirTable() {
		TableColumn<Class<? extends JAXBEntity>, String> dirs = new TableColumn<>();
		dirTable.getColumns().add(dirs);
		dirs.setCellValueFactory(callback -> new SimpleStringProperty(callback.getValue().getPackageName()));

		dirTable.getSelectionModel().selectedItemProperty().addListener((property, oldValue, newValue) -> {
			if (newValue == null || newValue.equals(oldValue))
				return;

			entityTable.getItems().clear();
			previewArea.setText("");
			try {
				entityTable.getItems().addAll(EntityLoader.INSTANCE.loadAllEntities(newValue));
			} catch (IOException e) {
				OwlLogger.registerException(1, e);
			}
		});
	}

	private void initKeyBind() {
		var selectionModel = entityTable.getSelectionModel();
		entityTable.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
			switch (keyEvent.getCode()) {
			case DELETE:
				if (selectionModel.isEmpty())
					return;
				deleteEntityAction(selectionModel.getSelectedItems());
				break;
			case A:
				if (keyEvent.isControlDown()) {
					selectionModel.selectAll();
				}
				break;
			default:
				break;
			}
		});
		entityTable.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
			switch (mouseEvent.getButton()) {
			case PRIMARY:
				if (mouseEvent.getClickCount() == 2) {
					if (selectionModel.isEmpty())
						return;
					editEntityAction(selectionModel.getSelectedItem());
				}
				break;

			default:
				break;
			}
		});
	}

	private void initEntityManagerButtons() {
		Button importEntity = new Button("Import");
		importEntity.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Import");
			chooser.getExtensionFilters().add(new ExtensionFilter("Owl", "*.owl"));
			List<File> filesChoose = chooser.showOpenMultipleDialog(getStage());

			if (filesChoose == null || filesChoose.size() == 0)
				return;
			List<Path> paths = filesChoose.stream().map(f -> f.toPath()).collect(Collectors.toList());
			importEntitiesAction(paths);
		});
		buttonBar.getButtons().add(importEntity);

		Button exportEntity = new Button("Export");
		exportEntity.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Export");
			File targetDir = chooser.showDialog(getStage());
			if (targetDir == null)
				return;

			exportEntitiesAction(targetDir.toPath());
		});
		exportEntity.setDisable(true);
		entityTable.getSelectionModel().selectedItemProperty().addListener((property, oldValue, newValue) -> {
			exportEntity.setDisable(entityTable.getSelectionModel().isEmpty());
		});
		buttonBar.getButtons().add(exportEntity);

	}

	private void deleteEntityAction(List<JAXBEntity> jaxbEntities) {
		List<JAXBEntity> entities = new ArrayList<>(jaxbEntities);
		for (JAXBEntity entity : entities) {
			EntityLoader.INSTANCE.deleteEntity(entity);
		}
	}

	private void editEntityAction(JAXBEntity jaxbEntitiy) {
		if (jaxbEntitiy instanceof Controllable) {
			try {
				((Controllable) jaxbEntitiy).getConfigController().show();
			} catch (IOException e) {
				OwlLogger.registerException(1, e);
			}
		}
	}

	private void duplicateEntityAction(List<JAXBEntity> jaxbEntities) {
		jaxbEntities.forEach(entity -> {
			try {
				JAXBEntity copyEntity = EntityLoader.INSTANCE.duplicateEntity(entity);
				copyEntity.setTitle(entity.getTitle() + "_(copy)");
			} catch (JAXBException | IOException e) {
				OwlLogger.registerException(1, e);
			}
		});
	}

	private void createEntityAction(Class<? extends JAXBEntity> target) {
		try {
			EntityLoader.INSTANCE.createEntity(target);
		} catch (JAXBException | IOException e) {
			OwlLogger.registerException(1, e);
		}
	}

	private void importEntitiesAction(List<Path> paths) {
		for (Path path : paths) {
			try {
				JAXBEntity entity = EntityLoader.INSTANCE.tryParseEntity(path);
				try {
					JAXBEntity newEntity = EntityLoader.INSTANCE.createEntity(entity.getClass());
					newEntity.syncWith(entity);
				} catch (JAXBException | IOException e) {
					OwlLogger.registerException(1, e);
				}

			} catch (FileNotFoundException e) {
				OwlLogger.registerException(1, e);
			} catch (JAXBEntityValidateException e) {
				OwlLogger.registerException(2, e);
			} catch (Nullable e) {
			}
		}

	}

	private void exportEntitiesAction(Path dir) {
		TableViewSelectionModel<JAXBEntity> selection = entityTable.getSelectionModel();
		if (selection.isEmpty())
			return;

		for (JAXBEntity entity : selection.getSelectedItems()) {
			Path out;
			if (entity.getTitle() != null && entity.getTitle() != "") {
				String fileName = entity.getTitle().replace(" ", "_").toLowerCase();
				out = dir.resolve(fileName + ".owl");
				if (Files.exists(out)) {
					out = dir.resolve(fileName + "_" + System.currentTimeMillis() + ".owl");
				}
			} else {
				out = dir.resolve(System.currentTimeMillis() + ".owl");
			}

			try {
				Files.copy(entity.getPath(), out);
			} catch (IOException e) {
				OwlLogger.registerException(1, e);
			}
		}
	}

}
