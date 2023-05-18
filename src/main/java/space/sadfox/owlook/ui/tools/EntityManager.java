package space.sadfox.owlook.ui.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import space.sadfox.owlook.ResourceTarget;
import space.sadfox.owlook.jaxb.EntityLoader;
import space.sadfox.owlook.jaxb.JAXBEntity;
import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.utils.ErrorLogger;
import space.sadfox.owlook.utils.ModuleLoader;
import space.sadfox.owlook.utils.Nullable;

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
		initEntityTable();
		initDirTable();
		initKeyBind();

		ModuleLoader.INSTANCE.loadModules().forEach(module -> {
			try {
				dirTable.getItems().addAll(module.getJaxbEntities());
			} catch (Nullable e) {
			}
		});
	}
	
	private void init() {
		getStage().setTitle("Entity Manager");
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

		MenuItem copy = new MenuItem("Copy");
		copy.setOnAction(event -> {
			copyEntityAction(selectionModel.getSelectedItems());
		});
		MenuItem paste = new MenuItem("Paste");
		paste.setOnAction(event -> {
			pasteEntityAction();
		});

		contextMenu.getItems().addAll(create, edit, duplicate, delete, copy, paste);

		contextMenu.setOnShowing(event -> {
			boolean visible = !selectionModel.isEmpty();
			edit.setVisible(visible);
			duplicate.setVisible(visible);
			delete.setVisible(visible);
			copy.setVisible(visible);
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
			entityTable.getItems().addAll(EntityLoader.INSTANCE.loadAllEntities(newValue));
		});
	}

	private void initKeyBind() {
		var selectionModel = entityTable.getSelectionModel();
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
			case C:
				if (selectionModel.isEmpty())
					return;
				if (keyEvent.isControlDown()) {
					copyEntityAction(selectionModel.getSelectedItems());
				}
				break;
			case V:
				if (selectionModel.isEmpty())
					return;
				if (keyEvent.isControlDown()) {
					// TODO:
					pasteEntityAction();
				}
				break;
			default:
				break;
			}
		});

	}

	private void deleteEntityAction(List<JAXBEntity> jaxbEntities) {
		// TODO:
		List<JAXBEntity> entities = new ArrayList<>(jaxbEntities);
		for (JAXBEntity entity : entities) {
			EntityLoader.INSTANCE.deleteEntity(entity);
		}
	}

	private void editEntityAction(JAXBEntity jaxbEntitiy) {
		try {
			jaxbEntitiy.getConfigController().show();
		} catch (IOException e) {
			ErrorLogger.registerException(e);
		} catch (Nullable e) {
		}
	}

	private void copyEntityAction(List<JAXBEntity> jaxbEntities) {
		// TODO:
	}

	private void duplicateEntityAction(List<JAXBEntity> jaxbEntities) {
		jaxbEntities.forEach(entity -> {
			try {
				JAXBEntity copyEntity = EntityLoader.INSTANCE.duplicateEntity(entity);
				copyEntity.setTitle(entity.getTitle() + "_(copy)");
			} catch (JAXBException | IOException e) {
				ErrorLogger.registerException(e);
			}
		});
	}

	private void pasteEntityAction() {
		// TODO:
	}

	private void openEntityDir(Class<? extends JAXBEntity> target) {
		// TODO:
	}

	private void createEntityAction(Class<? extends JAXBEntity> target) {
		try {
			EntityLoader.INSTANCE.createEntity(target);
		} catch (JAXBException | IOException e) {
			ErrorLogger.registerException(e);
		}
	}

}
