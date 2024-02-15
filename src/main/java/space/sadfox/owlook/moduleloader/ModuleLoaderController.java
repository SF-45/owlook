package space.sadfox.owlook.moduleloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
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
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import space.sadfox.owlook.OwlookConfiguration;
import space.sadfox.owlook.ResourceTarget;
import space.sadfox.owlook.base.moduleapi.OwlookModulePack;
import space.sadfox.owlook.base.moduleapi.OwlookModulePacks;
import space.sadfox.owlook.base.moduleapi.VersionFormat;
import space.sadfox.owlook.moduleloader.ModuleLoader.LoadReport;
import space.sadfox.owlook.ui.MainStage;
import space.sadfox.owlook.ui.base.FXMLController;
import space.sadfox.owlook.ui.tools.MessageBox;
import space.sadfox.owlook.utils.Logger;
import space.sadfox.owlook.utils.ProjectPath;
import space.sadfox.owlook.utils.StageFactory;

public class ModuleLoaderController extends FXMLController {

	private enum TableEntityStatus {
		OK("OK"), ERROR("Error"), NOT_FOUND("Not Found"), DISABLE("Disable"), READY("Ready");

		final String title;

		private TableEntityStatus(String title) {
			this.title = title;
		}

		@Override
		public String toString() {
			return title;
		}
	}

	private class TableEntity {
		final OwlookModulePack owlookModulePack;

		final StringProperty name = new SimpleStringProperty();
		final StringProperty moduleName = new SimpleStringProperty();
		final StringProperty description = new SimpleStringProperty();
		final ObjectProperty<VersionFormat> version = new SimpleObjectProperty<>();
		final StringProperty massage = new SimpleStringProperty("");
		final BooleanProperty enable = new SimpleBooleanProperty(false);
		final ObjectProperty<TableEntityStatus> status = new SimpleObjectProperty<>(TableEntityStatus.READY);

		TableEntity(OwlookModulePack owlookModulePack) {
			this.owlookModulePack = owlookModulePack;

			name.set(owlookModulePack.MODILE_INFO.name());
			description.set(owlookModulePack.MODILE_INFO.description());
			version.set(owlookModulePack.MODILE_INFO.version());
			moduleName.set(owlookModulePack.MODILE_INFO.moduleName());

			enable.set(config.getModules().contains(moduleName.get()));
			enable.addListener((property, oldValue, newValue) -> {
				if (newValue && !config.getModules().contains(moduleName.get())) {
					config.getModules().add(moduleName.get());
					status.set(TableEntityStatus.READY);
				} else {
					config.getModules().remove(moduleName.get());
					status.set(TableEntityStatus.DISABLE);
				}
				testLaunch();
			});
			update();
		}

		TableEntity(String owlookModuleName) {
			this.owlookModulePack = null;

			name.set(owlookModuleName);
			moduleName.set(owlookModuleName);
			status.set(TableEntityStatus.NOT_FOUND);
			enable.addListener((property, oldValue, newValue) -> {
				if (newValue) {
					enable.set(false);
				}
			});
			update();
		}

		void update() {
			update(null);
		}

		void update(LoadReport loadReport) {
			if (status.get().equals(TableEntityStatus.NOT_FOUND))
				return;
			if (!enable.get()) {
				status.set(TableEntityStatus.DISABLE);
				massage.set("");
				return;
			}
			if (loadReport == null)
				return;
			for (ModuleLoadInfo info : loadReport.getModuleLoadInfoList()) {
				if (info.PACK.LOCATION.equals(owlookModulePack.LOCATION)) {
					switch (info.STATUS) {
					case OK:
						status.set(TableEntityStatus.OK);
						break;
					case ERROR:
						status.set(TableEntityStatus.ERROR);
						break;
					default:
						break;
					}
					massage.set(info.MASSAGE);

				}
			}
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
				if (tableEntity.status.get().equals(TableEntityStatus.OK)) {
					// TODO: Окно с просмотрм подробного описания модуля
				} else {
					MessageBox messageBox = new MessageBox(AlertType.ERROR);
					messageBox.setTitle("Module " + tableEntity.name.get() + " not OK");
					messageBox.setHeaderText("Module [" + tableEntity.name.get()
							+ "] is not ready because its state is " + tableEntity.status.get().toString());
					messageBox.showAndWait();
				}
			});
			MenuItem remove = new MenuItem("Remove");
			remove.setOnAction(event -> {
				Dialog<ButtonType> dialog = new Dialog<>();
				dialog.getDialogPane().getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
				dialog.setTitle("Deletion confirmation");
				dialog.setHeaderText("Are you sure you want to remove the module " + tableEntity.name.get() + " ?");

				Optional<ButtonType> choose = dialog.showAndWait();

				if (choose.isPresent() && choose.get().equals(ButtonType.YES)) {
					removeTableEntity(tableEntity);
				}
			});
			this.getItems().add(remove);

		}

	}

	@FXML
	private MenuItem importModulesButton;

	@FXML
	private Button launchButton;

	@FXML
	private TableView<TableEntity> moduleTable;

	@FXML
	private AnchorPane root;

	@FXML
	private CheckBox skipModuleManagerCheckBox;

	private final OwlookConfiguration config = OwlookConfiguration.instance();

	private final ObservableList<TableEntity> tableEntities = FXCollections.observableArrayList();

	public ModuleLoaderController() throws IOException {
		super(ResourceTarget.class.getResource("fxml/module-loader.fxml"));

		init();

		List<OwlookModulePack> findPacks = OwlookModulePacks
				.getModulePacks(OwlookModulePacks.findModuleFiles(ProjectPath.MODULE.getPath(), 1));
		findPacks.stream().map(TableEntity::new).forEach(tableEntities::add);

		config.getModules().stream().filter(moduleName -> {
			return tableEntities.stream().noneMatch(tableEntity -> tableEntity.moduleName.get().equals(moduleName));
		}).forEach(moduleName -> tableEntities.add(new TableEntity(moduleName)));

		testLaunch();
	}

	private void init() {
		stageTitle.set("Module Manage");
		skipModuleManagerCheckBox.setSelected(config.isSkipModuleManage());
		config.skipModuleManageProperty().bindBidirectional(skipModuleManagerCheckBox.selectedProperty());
		initModuleTable();
		launchButton.setOnAction(this::launch);

		root.setOnDragOver(dragEvent -> {
			Dragboard dragboard = dragEvent.getDragboard();
			if (dragboard.hasFiles()) {
				boolean modulesDetect = dragboard.getFiles().stream().map(File::getName)
						.allMatch(fileName -> fileName.endsWith(".owlm"));
				if (modulesDetect) {
					dragEvent.acceptTransferModes(TransferMode.ANY);
					dragEvent.consume();
				}
			}
		});
		root.setOnDragDropped(dragEvent -> {
			Dragboard dragboard = dragEvent.getDragboard();
			dragboard.getFiles().stream().map(File::toPath).forEach(this::importModulePack);
		});

		importModulesButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new ExtensionFilter("OwlookModule", "*.owlm"));

			List<File> selectedModules = fileChooser.showOpenMultipleDialog(StageFactory.INSTANCE.getCurrentStage());

			if (selectedModules != null) {
				selectedModules.stream().map(File::toPath).forEach(this::importModulePack);
			}
		});
	}

	private void initModuleTable() {
		moduleTable.setItems(tableEntities);
		moduleTable.setEditable(true);

		TableColumn<TableEntity, Boolean> moduleEnable = new TableColumn<>("Enable");
		moduleEnable.setCellValueFactory(cellData -> cellData.getValue().enable);
		moduleEnable.setCellFactory(CheckBoxTableCell.forTableColumn(moduleEnable));
		moduleTable.getColumns().add(moduleEnable);

		TableColumn<TableEntity, TableEntityStatus> moduleState = new TableColumn<>("State");
		moduleState.setCellValueFactory(cellData -> cellData.getValue().status);
		moduleTable.getColumns().add(moduleState);

		TableColumn<TableEntity, String> moduleName = new TableColumn<>("Name");
		moduleName.setCellValueFactory(cellData -> cellData.getValue().name);
		moduleTable.getColumns().add(moduleName);

		TableColumn<TableEntity, String> moduleDescription = new TableColumn<>("Description");
		moduleDescription.setCellValueFactory(cellData -> cellData.getValue().description);
		moduleTable.getColumns().add(moduleDescription);

		TableColumn<TableEntity, VersionFormat> moduleVersion = new TableColumn<>("Version");
		moduleVersion.setCellValueFactory(cellData -> cellData.getValue().version);
		moduleTable.getColumns().add(moduleVersion);

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

		TableColumn<TableEntity, String> moduleErrors = new TableColumn<>("Massage");
		moduleErrors.setCellValueFactory(cellData -> cellData.getValue().massage);
		moduleTable.getColumns().add(moduleErrors);

	}

	private void updateTableEtities(LoadReport loadReport) {
		tableEntities.forEach(e -> e.update(loadReport));
	}

	private List<TableEntity> filteredTableEntities(TableEntityStatus... status) {
		List<TableEntityStatus> statusList = Arrays.asList(status);
		return tableEntities.filtered(tableEntity -> statusList.contains(tableEntity.status.get()));
	}

	private List<OwlookModulePack> filteredModulePacks(TableEntityStatus... status) {
		return filteredTableEntities(status).stream().map(tableEntity -> tableEntity.owlookModulePack)
				.collect(Collectors.toList());
	}

	private void testLaunch() {
		LoadReport loadReport = ModuleLoader.INSTANCE
				.testBoot(filteredModulePacks(TableEntityStatus.OK, TableEntityStatus.ERROR, TableEntityStatus.READY));
		updateTableEtities(loadReport);
	}

	private void launch(ActionEvent actionEvent) {
		Stage thisStage = StageFactory.INSTANCE.getCurrentStage();
		if (launch()) {
			thisStage.close();
		}

	}

	public boolean launch() {
		List<TableEntity> errorTableEntities = filteredTableEntities(TableEntityStatus.ERROR,
				TableEntityStatus.NOT_FOUND);

		if (errorTableEntities.size() > 0) {
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
			dialog.setTitle("Module loading error");
			dialog.setHeaderText(errorTableEntities.size() + " modules were not loaded. Start anyway?");

			StringBuilder builder = new StringBuilder();
			errorTableEntities.forEach(errorTableEntity -> {
				builder.append(errorTableEntity.name.get()).append("[" + errorTableEntity.status.get() + "]")
						.append("\n");
			});
			dialog.setContentText(builder.toString());

			Optional<ButtonType> choose = dialog.showAndWait();

			if (choose.isEmpty() || choose.get().equals(ButtonType.NO)) {
				return false;
			}

		}
		LoadReport loadReport = ModuleLoader.INSTANCE.boot(filteredModulePacks(TableEntityStatus.OK));
		updateTableEtities(loadReport);

		if (ModuleLoader.INSTANCE.isBoot()) {
			try {
				ModuleLoader.INSTANCE.initOwlookModules(true);
				MainStage mainStage = new MainStage();
        mainStage.getStage().setResizable(false);
        mainStage.getStage().setWidth(540d);
        mainStage.getStage().setHeight(625d);
				mainStage.show();
				return true;
			} catch (IOException e) {
				Logger.registerException(0, e);
			}
		}
		return false;

	}

	private void removeTableEntity(TableEntity tableEntity) {

		try {
			if (!tableEntity.status.get().equals(TableEntityStatus.NOT_FOUND)) {
				removeModulePack(tableEntity.owlookModulePack);
			}
			config.getModules().removeIf(moduleName -> moduleName.equals(tableEntity.moduleName.get()));
			tableEntities.remove(tableEntity);
			testLaunch();
		} catch (IOException e) {
			Logger.registerException(1, e);

			MessageBox messageBox = new MessageBox(AlertType.ERROR);
			messageBox.setTitle("Deletion error");
			messageBox.setHeaderText("Module removal error " + tableEntity.name.get());
			messageBox.setContentText(e.getMessage());
			messageBox.showAndWait();
		}
	}

	private void removeModulePack(OwlookModulePack owlookModulePack) throws IOException {
		if (owlookModulePack.isOpened()) {
			owlookModulePack.close();
		}
		Files.deleteIfExists(owlookModulePack.LOCATION);
	}

	private void importModulePack(Path moduleFile) {
		try {
			try (OwlookModulePack newPack = new OwlookModulePack(moduleFile)) {

				if (!newPack.MODILE_INFO.version().compatibleWith(config.getVersion())) {
					MessageBox message = new MessageBox(AlertType.ERROR);
					message.setTitle("Version incompatibility [" + newPack.MODILE_INFO.name() + "-"
							+ newPack.MODILE_INFO.version() + "]");
					message.setHeaderText("The module version is incompatible with the version Owlook");
					message.showAndWait();
					return;
				}

				List<TableEntity> sameEntities = tableEntities
						.filtered(tableEntity -> tableEntity.moduleName.get().equals(newPack.MODILE_INFO.moduleName()));

				if (sameEntities.size() > 0) {
					sameEntities = new ArrayList<>(sameEntities);
					sameEntities.sort(
							Comparator.<TableEntity, VersionFormat>comparing(te1 -> te1.version.get()).reversed());
					OwlookModulePack samePack = sameEntities.get(0).owlookModulePack;
					Dialog<ButtonType> dialog = new Dialog<>();
					dialog.getDialogPane().getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
					dialog.setTitle("The module is already installed [" + newPack.MODILE_INFO.name() + "-"
							+ newPack.MODILE_INFO.version() + "]");
					Optional<ButtonType> answer = Optional.empty();

					if (samePack.MODILE_INFO.version().compareTo(newPack.MODILE_INFO.version()) > 0) {
						dialog.setHeaderText("The version of the installed module is higher. Downgrade?");
						dialog.setContentText(samePack.MODILE_INFO.version() + " -> " + newPack.MODILE_INFO.version());
						answer = dialog.showAndWait();
					} else if (samePack.MODILE_INFO.version().compareTo(newPack.MODILE_INFO.version()) < 0) {
						dialog.setHeaderText("The version of the installed module is lower. Upgrade?");
						dialog.setContentText(samePack.MODILE_INFO.version() + " -> " + newPack.MODILE_INFO.version());
						answer = dialog.showAndWait();
					}

					if (answer.isEmpty() || answer.get().equals(ButtonType.NO)) {
						return;
					}
					sameEntities.forEach(this::removeTableEntity);
				}

			}

			Path newModuleFile = ProjectPath.MODULE.getPath().resolve(moduleFile.getFileName());
			Files.copy(moduleFile, newModuleFile);
			tableEntities.add(new TableEntity(new OwlookModulePack(newModuleFile)));
			testLaunch();
		} catch (IOException e) {
			Logger.registerException(1, e);
			MessageBox messageBox = new MessageBox(AlertType.ERROR);
			messageBox.setTitle("Import error");
			messageBox.setHeaderText("Module import error: " + moduleFile);
			messageBox.setContentText(e.getMessage());
			messageBox.showAndWait();

		}
	}
}
