package space.sadfox.owlook.moduleapi;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.module.ResolutionException;
import java.lang.module.ResolvedModule;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import space.sadfox.owlook.OwlookModuleProvider;
import space.sadfox.owlook.ResourceTarget;
import space.sadfox.owlook.configuration.OwlookConfigurationEntity;
import space.sadfox.owlook.ui.MainStage;
import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.ui.tools.MessageBox;
import space.sadfox.owlook.utils.OwlLogger;
import space.sadfox.owlook.utils.ProjectPath;
import space.sadfox.owlook.utils.StageFactory;

public class ModuleLoaderController extends Controller {

	private enum State {
		ENABLE("Enable", 0), DISABLE("Disable", 1), NOT_FOUND("Not Found", 3);

		final String title;
		final int ORDER;

		private State(String title, int order) {
			this.title = title;
			ORDER = order;
		}

		@Override
		public String toString() {
			return title;
		}

	}

	private class TableEntity implements Comparable<State> {

		final OwlookModule owlookModule;
		final Module module;
		final ModuleReference moduleReference;

		final StringProperty nameProperty = new SimpleStringProperty();
		final ObjectProperty<State> stateProperty = new SimpleObjectProperty<>();

		TableEntity(OwlookModule owlookModule) {
			this.owlookModule = owlookModule;
			this.module = owlookModule.getClass().getModule();

			Optional<ResolvedModule> res = module.getLayer().configuration().findModule(module.getName());
			if (res.isPresent()) {
				moduleReference = res.get().reference();
			} else {
				moduleReference = null;
			}

			nameProperty.set(module.getName());

			OwlookConfigurationEntity config = OwlookModuleProvider.getConfig();
			stateProperty.addListener((property, oldValue, newValue) -> {
				switch (newValue) {
				case ENABLE:
					if (!config.getModules().contains(module.getName())) {
						config.getModules().add(module.getName());
					}
					break;
				case DISABLE:
					config.getModules().remove(module.getName());
					break;
				default:
					break;
				}
			});
		}

		TableEntity(String moduleName) {
			owlookModule = null;
			module = null;
			moduleReference = null;

			nameProperty.set(moduleName);
			stateProperty.set(State.NOT_FOUND);
		}

		void removeModule() {
			if (stateProperty.get().equals(State.NOT_FOUND)) {
				OwlookModuleProvider.getConfig().getModules().remove(nameProperty.get());
				tableEntities.remove(this);
			}
			// TODO: Удаление модуля
		}

		@Override
		public int compareTo(State o) {
			int thisOrder = stateProperty.get().ORDER;
			int compareOrder = o.ORDER;

			if (thisOrder < compareOrder)
				return 1;
			else if (thisOrder > compareOrder)
				return -1;
			else
				return 0;

		}
	}

	private class TableEntitySplitButton extends SplitMenuButton {
		final TableEntity tableEntity;

		public TableEntitySplitButton(TableEntity tableEntity) {
			this.tableEntity = tableEntity;
			if (tableEntity != null) {
				init();
			}

		}

		private void init() {
			updateText();
			tableEntity.stateProperty.addListener((property, oldValue, newValue) -> {
				updateText();
			});
			setOnAction(event -> {
				switch (tableEntity.stateProperty.get()) {
				case ENABLE:
					tableEntity.stateProperty.set(State.DISABLE);
					break;
				case DISABLE:
					tableEntity.stateProperty.set(State.ENABLE);
					break;
				default:
					break;
				}
			});
			MenuItem remove = new MenuItem("Remove");
			remove.setOnAction(event -> {
				tableEntity.removeModule();
			});
			this.getItems().add(remove);
		}

		private void updateText() {
			switch (tableEntity.stateProperty.get()) {
			case ENABLE:
				this.setText("Disable");
				break;
			case DISABLE:
				this.setText("Enable");
				break;
			case NOT_FOUND:
				this.setText("Not Found");
				break;
			default:
				break;
			}
		}

	}

	@FXML
	private Button launchButton;

	@FXML
	private TextArea moduleDescriptionTextArea;

	@FXML
	private TableView<TableEntity> moduleTable;

	private final ObservableList<TableEntity> tableEntities = FXCollections.observableArrayList();

	public ModuleLoaderController() throws IOException {
		super(ResourceTarget.class.getResource("fxml/module-loader.fxml"));
		init();

		ModuleFinder pluginsFinder = ModuleFinder.of(ProjectPath.MODULE.getPath());
		ModuleFinder libFinder = ModuleFinder.of(ProjectPath.MODULE_LIB.getPath());

		List<String> plugins = pluginsFinder.findAll().stream().map(ModuleReference::descriptor)
				.map(ModuleDescriptor::name).collect(Collectors.toList());

		try {
			Configuration tempConfiguration = ModuleLayer.boot().configuration().resolve(pluginsFinder, libFinder,
					plugins);
			ModuleLayer tempLayer = ModuleLayer.boot().defineModulesWithOneLoader(tempConfiguration,
					ClassLoader.getSystemClassLoader());
			OwlookConfigurationEntity config = OwlookModuleProvider.getConfig();

			ServiceLoader.load(tempLayer, OwlookModule.class).stream().forEach(provider -> {
				TableEntity tableEntity = new TableEntity(provider.get());
				if (config.getModules().contains(tableEntity.nameProperty.get())) {
					tableEntity.stateProperty.set(State.ENABLE);
				} else {
					tableEntity.stateProperty.set(State.DISABLE);
				}
				tableEntities.add(tableEntity);
			});

			config.getModules().stream().filter(moduleName -> {
				return tableEntities.stream()
						.noneMatch(tableEntity -> tableEntity.nameProperty.get().equals(moduleName));
			}).forEach(moduleName -> tableEntities.add(new TableEntity(moduleName)));

		} catch (FindException | ResolutionException e) {
			OwlLogger.registerException(1, e);

			MessageBox messageBox = new MessageBox(AlertType.ERROR);
			messageBox.setTitle("Error loading modules");
			messageBox.setHeaderText("An error occurred while loading modules.");
			messageBox.setContentText(e.getMessage());
			messageBox.showAndWait();

			StageFactory.INSTANCE.getCurrentStage().close();
		}

	}

	private void init() {
		stageTitle.set("Module Manage");
		initModuleTable();
		launchButton.setOnAction(this::launch);
	}

	private void initModuleTable() {
		moduleTable.setItems(tableEntities);

		moduleTable.getSelectionModel().selectedItemProperty().addListener((property, oldValue, newValue) -> {
			if (newValue == null) {
				moduleDescriptionTextArea.setText("");
			} else {
				moduleDescriptionTextArea.setText(newValue.owlookModule.getModuleDescription());
			}
		});

		TableColumn<TableEntity, State> moduleState = new TableColumn<>("State");
		moduleState.setCellValueFactory(cellData -> cellData.getValue().stateProperty);
		moduleTable.getColumns().add(moduleState);

		TableColumn<TableEntity, String> moduleName = new TableColumn<>("Name");
		moduleName.setCellValueFactory(cellData -> cellData.getValue().nameProperty);
		moduleTable.getColumns().add(moduleName);

		TableColumn<TableEntity, TableEntity> moduleAction = new TableColumn<>("Action");
		moduleAction.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
		moduleAction.setCellFactory(callback -> {
			return new TableCell<>() {

				@Override
				public void updateItem(TableEntity item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setText(null);
						setGraphic(null);
					} else {
						setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						setGraphic(new TableEntitySplitButton(item));
					}
				}
			};
		});
		moduleTable.getColumns().add(moduleAction);

		TableColumn<TableEntity, String> moduleDescription = new TableColumn<>("Description");
		moduleDescription.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().owlookModule.getShortModuleDescription()));

	}

	private void launch(ActionEvent actionEvent) {
		Stage thisStage = StageFactory.INSTANCE.getCurrentStage();
		if (launch()) {
			thisStage.close();
		}

	}

	public boolean launch() {
		List<TableEntity> enableTableEntities = tableEntities
				.filtered(tableEntity -> tableEntity.stateProperty.get().equals(State.ENABLE));
		List<Path> enableModulePaths = enableTableEntities.stream()
				.map(tableEntity -> Path.of(tableEntity.moduleReference.location().get())).collect(Collectors.toList());
		List<String> enablePlugins = enableTableEntities.stream().map(tableEntity -> tableEntity.module.getName())
				.collect(Collectors.toList());

		ModuleFinder pluginsFinder = ModuleFinder.of(enableModulePaths.toArray(new Path[enableModulePaths.size()]));
		ModuleFinder libFinder = ModuleFinder.of(ProjectPath.MODULE_LIB.getPath());

		try {
			Configuration configuration = ModuleLayer.boot().configuration().resolve(pluginsFinder, libFinder,
					enablePlugins);
			ModuleLoader.INSTANCE.setModuleLayer(
					ModuleLayer.boot().defineModulesWithOneLoader(configuration, ClassLoader.getSystemClassLoader()));

			ModuleLoader.INSTANCE.loadModules().forEach(OwlookModule::initModule);
			MainStage mainStage = new MainStage();
			mainStage.show();
			return true;

		} catch (FindException | ResolutionException e) {
			OwlLogger.registerException(1, e);

			MessageBox messageBox = new MessageBox(AlertType.ERROR);
			messageBox.setTitle("Error loading modules");
			messageBox.setHeaderText("An error occurred while loading modules.");
			messageBox.setContentText(e.getMessage());
			messageBox.showAndWait();
		} catch (IOException e) {
			OwlLogger.registerException(1, e);
		}
		return false;
	}

}
