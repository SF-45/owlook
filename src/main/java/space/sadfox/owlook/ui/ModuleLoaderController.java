package space.sadfox.owlook.ui;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.FindException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.module.ResolutionException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import space.sadfox.owlook.OwlookConfiguration;
import space.sadfox.owlook.ResourceTarget;
import space.sadfox.owlook.base.moduleapi.OwlookModule;
import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.ui.tools.MessageBox;
import space.sadfox.owlook.utils.ModuleLoader;
import space.sadfox.owlook.utils.Nullable;
import space.sadfox.owlook.utils.OwlLogger;
import space.sadfox.owlook.utils.ProjectPath;
import space.sadfox.owlook.utils.StageFactory;

public class ModuleLoaderController extends Controller {
	private enum State {
		OK("OK", 0), NOT_OWLMODULE("Not Owl-Module", 1), ERROR("Error", 2), NOT_FOUND("Not Found", 3),
		DISABLE("Disable", 4);

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

		private OwlookModule owlookModule;
		final ModuleReference moduleReference;

		final ReadOnlyStringProperty nameProperty;
		final ObjectProperty<State> stateProperty = new SimpleObjectProperty<>(State.DISABLE);
		final BooleanProperty enableProperty = new SimpleBooleanProperty(false);
		final StringProperty errorsProperty = new SimpleStringProperty();

		TableEntity(ModuleReference moduleReference) {

			this.moduleReference = moduleReference;

			nameProperty = new SimpleStringProperty(moduleReference.descriptor().name());

			enableProperty.set(config.getModules().contains(nameProperty.get()));
			enableProperty.addListener((property, oldValue, newValue) -> {
				if (newValue && !config.getModules().contains(nameProperty.get())) {
					config.getModules().add(nameProperty.get());
				} else {
					config.getModules().remove(nameProperty.get());
				}
				validateModules();
			});
		}

		TableEntity(String moduleName) {
			moduleReference = null;

			enableProperty.addListener((property, oldValue, newValue) -> {
				if (newValue)
					enableProperty.set(false);
			});
			nameProperty = new SimpleStringProperty(moduleName);
			stateProperty.set(State.NOT_FOUND);
		}

		void validate(ModuleFinder moduleFinder) {
			if (stateProperty.get().equals(State.NOT_FOUND)) {
				return;
			}
			if (!enableProperty.get()) {
				stateProperty.set(State.DISABLE);
				errorsProperty.set("");
				return;
			}
			try {
				Configuration configuration = ModuleLayer.boot().configuration().resolve(moduleFinder, libFinder,
						Arrays.asList(nameProperty.get()));
				ModuleLayer moduleLayer = ModuleLayer.boot().defineModulesWithOneLoader(configuration,
						ClassLoader.getSystemClassLoader());
				List<Provider<OwlookModule>> owlModuleProviders = ServiceLoader.load(moduleLayer, OwlookModule.class)
						.stream().collect(Collectors.toList());

				List<OwlookModule> owlookModules = owlModuleProviders.stream()
						.filter(provider -> provider.type().getModule().getName().equals(nameProperty.get()))
						.map(Provider::get).collect(Collectors.toList());

				if (owlookModules.size() == 0) {
					errorsProperty.set("This module not provide OwlookModule");
					stateProperty.set(State.NOT_OWLMODULE);
				} else if (owlookModules.size() > 1) {
					errorsProperty.set("This module provide more than one OwlookModule");
					stateProperty.set(State.NOT_OWLMODULE);
				} else {
					owlookModule = owlookModules.get(0);
					stateProperty.set(State.OK);
					errorsProperty.set("");
				}

			} catch (FindException | ResolutionException e) {
				OwlLogger.registerException(3, e);
				errorsProperty.set(e.getMessage());
				stateProperty.set(State.ERROR);
			}
		}
		
		OwlookModule getOwlookModule() throws Nullable {
			if (owlookModule == null) throw new Nullable();
			return owlookModule;
		}

		void removeModule() {
			if (stateProperty.get().equals(State.NOT_FOUND)) {
				config.getModules().remove(nameProperty.get());
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
			setText("Description");

			setOnAction(event -> {
				if (tableEntity.stateProperty.get().equals(State.OK)) {
					// TODO: Окно с просмотрм подробного описания модуля
				} else {
					MessageBox messageBox = new MessageBox(AlertType.ERROR);
					messageBox.setTitle("Module " + tableEntity.nameProperty.get() + " not OK");
					messageBox.setHeaderText("Module [" + tableEntity.nameProperty.get()
							+ "] is not ready because its state is " + tableEntity.stateProperty.get().toString());
					messageBox.showAndWait();
				}
			});
			MenuItem remove = new MenuItem("Remove");
			remove.setOnAction(event -> {
				tableEntity.removeModule();
			});
			this.getItems().add(remove);
			
//			MenuItem pack = new MenuItem("Pack");
//			pack.setOnAction(event -> {
//				DirectoryChooser chooser = new DirectoryChooser();
//				File chooseDir = chooser.showDialog(StageFactory.INSTANCE.getCurrentStage());
//				
//				if (chooseDir == null)
//					return;
//				OwlookModulePacker packer;
//				try {
//					List <OwlookModule> owlookModules = new ArrayList<>();
//					tableEntities.forEach(te -> {
//						try {
//							owlookModules.add(te.getOwlookModule());
//						} catch (Nullable e) {
//							e.printStackTrace();
//						}
//					});
//					packer = new OwlookModulePacker(tableEntity.getOwlookModule());
//					packer.pack(chooseDir.toPath());
//					
//					MessageBox messageBox = new MessageBox(AlertType.INFORMATION);
//					messageBox.setTitle("Package complete");
//					messageBox.setHeaderText("Package Module [" + tableEntity.nameProperty.get()
//							+ "] comlete");
//					messageBox.showAndWait();
//				} catch (PackageException e) {
//					OwlLogger.registerException(0, e.getCause());
//				} catch (Nullable e) {
//					
//				}
//			});
//			this.getItems().add(pack);
//			
//			MenuItem packAll = new MenuItem("Pack All");
//			packAll.setOnAction(event -> {
//				DirectoryChooser chooser = new DirectoryChooser();
//				File chooseDir = chooser.showDialog(StageFactory.INSTANCE.getCurrentStage());
//				
//				if (chooseDir == null)
//					return;
//				OwlookModulePacker packer;
//				try {
//					List <OwlookModule> owlookModules = new ArrayList<>();
//					tableEntities.forEach(te -> {
//						try {
//							owlookModules.add(te.getOwlookModule());
//						} catch (Nullable e) {
//							e.printStackTrace();
//						}
//					});
//					packer = new OwlookModulePacker(owlookModules.toArray(new OwlookModule[0]));
//					packer.pack(chooseDir.toPath());
//					
//					MessageBox messageBox = new MessageBox(AlertType.INFORMATION);
//					messageBox.setTitle("Package complete");
//					messageBox.setHeaderText("Package Module [" + tableEntity.nameProperty.get()
//							+ "] comlete");
//					messageBox.showAndWait();
//				} catch (PackageException e) {
//					OwlLogger.registerException(0, e.getCause());
//				}
//				
//			});
//			this.getItems().add(packAll);
		}

	}

	@FXML
	private Button launchButton;

	@FXML
	private TextArea moduleDescriptionTextArea;

	@FXML
	private TableView<TableEntity> moduleTable;

	@FXML
	private CheckBox skipModuleManagerCheckBox;

	private final ObservableList<TableEntity> tableEntities = FXCollections.observableArrayList();

	private final ModuleFinder pluginsFinder = ModuleFinder.of(ProjectPath.MODULE.getPath());
	private final ModuleFinder libFinder = ModuleFinder.of(ProjectPath.MODULE_LIB.getPath());
	private final OwlookConfiguration config = OwlookConfiguration.instance();

	public ModuleLoaderController() throws IOException {
		super(ResourceTarget.class.getResource("fxml/module-loader.fxml"));
		init();
		reloadModules();
	}

	private void reloadModules() {
		tableEntities.clear();
		for (ModuleReference moduleReference : pluginsFinder.findAll()) {
			TableEntity tableEntity = new TableEntity(moduleReference);
			tableEntities.add(tableEntity);
		}
		
		config.getModules().stream().filter(moduleName -> {
			return tableEntities.stream()
					.noneMatch(tableEntity -> tableEntity.nameProperty.get().equals(moduleName));
		}).forEach(moduleName -> tableEntities.add(new TableEntity(moduleName)));
		validateModules();
	}

	private void validateModules() {
		ModuleFinder enableModuleFinder = getEnableModuleFinder();
		tableEntities.forEach(tableEntity -> tableEntity.validate(enableModuleFinder));
	}

	private ModuleFinder getEnableModuleFinder() {
		List<ModuleReference> allModuleReferences = pluginsFinder.findAll().stream().collect(Collectors.toList());

		List<ModuleReference> enableModuleReferences = allModuleReferences.stream()
				.filter(moduleReference -> config.getModules().contains(moduleReference.descriptor().name()))
				.collect(Collectors.toList());

		List<Path> enableModulePaths = enableModuleReferences.stream()
				.map(moduleReference -> Path.of(moduleReference.location().get())).collect(Collectors.toList());

		return ModuleFinder.of(enableModulePaths.toArray(new Path[enableModulePaths.size()]));
	}

	private void init() {
		stageTitle.set("Module Manage");
		skipModuleManagerCheckBox.setSelected(config.isSkipModuleManage());
		config.skipModuleManageProperty().bindBidirectional(skipModuleManagerCheckBox.selectedProperty());
		initModuleTable();
		launchButton.setOnAction(this::launch);
	}

	private void initModuleTable() {
		moduleTable.setItems(tableEntities);
		moduleTable.setEditable(true);

		TableColumn<TableEntity, Boolean> moduleEnable = new TableColumn<>("Enable");
		moduleEnable.setCellValueFactory(cellData -> cellData.getValue().enableProperty);
		moduleEnable.setCellFactory(CheckBoxTableCell.forTableColumn(moduleEnable));
		moduleTable.getColumns().add(moduleEnable);

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

		TableColumn<TableEntity, String> moduleErrors = new TableColumn<>("Error");
		moduleErrors.setCellValueFactory(cellData -> cellData.getValue().errorsProperty);
		moduleTable.getColumns().add(moduleErrors);

	}

	private void launch(ActionEvent actionEvent) {
		Stage thisStage = StageFactory.INSTANCE.getCurrentStage();
		if (launch()) {
			thisStage.close();
		}

	}

	public boolean launch() {
		List<TableEntity> errorTableEntities = tableEntities
				.filtered(tableEntity -> !tableEntity.stateProperty.get().equals(State.OK))
				.filtered(tableEntity -> !tableEntity.stateProperty.get().equals(State.DISABLE));

		if (errorTableEntities.size() > 0) {
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
			dialog.setTitle("Module loading error");
			dialog.setHeaderText(errorTableEntities.size() + " modules were not loaded. Start anyway?");
			
			StringBuilder builder = new StringBuilder();
			errorTableEntities.forEach(errorTableEntity -> {
				builder.append(errorTableEntity.nameProperty.get())
						.append("[" + errorTableEntity.stateProperty.get() + "]").append("\n");
			});
			dialog.setContentText(builder.toString());
			
			Optional<ButtonType> choose = dialog.showAndWait();
			
			if (choose.isEmpty() || choose.get().equals(ButtonType.NO)) {
				return false;
			}
			
		}

		List<TableEntity> enableTableEntities = tableEntities
				.filtered(tableEntity -> tableEntity.stateProperty.get().equals(State.OK));
		List<Path> enableModulePaths = enableTableEntities.stream()
				.map(tableEntity -> Path.of(tableEntity.moduleReference.location().get())).collect(Collectors.toList());
		List<String> enablePlugins = enableTableEntities.stream().map(tableEntity -> tableEntity.nameProperty.get())
				.collect(Collectors.toList());

		ModuleFinder enablePluginsFinder = ModuleFinder
				.of(enableModulePaths.toArray(new Path[enableModulePaths.size()]));

		try {
			Configuration configuration = ModuleLayer.boot().configuration().resolve(enablePluginsFinder, libFinder,
					enablePlugins);

			ModuleLoaderUILink.setModuleLayer(ModuleLayer.boot().defineModulesWithOneLoader(configuration, ClassLoader.getSystemClassLoader()));
			ModuleLoader.INSTANCE.loadModules().forEach(OwlookModule::initModule);
			MainStage mainStage = new MainStage();
			mainStage.show();
			return true;

		} catch (IOException e) {
			OwlLogger.registerException(0, e);
		}
		return false;
	}
}
