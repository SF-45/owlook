package space.sadfox.owlook.owlery;

import static space.sadfox.owlook.owlery.OwleryConfig.DATE_COLUMN_HEAD;
import static space.sadfox.owlook.owlery.OwleryConfig.ID_COLUMN_HEAD;
import static space.sadfox.owlook.owlery.OwleryConfig.MODULE_COLUMN_HEAD;
import static space.sadfox.owlook.owlery.OwleryConfig.OWL_NAME_COLUMN_HEAD;
import static space.sadfox.owlook.owlery.OwleryConfig.TITLE_COLUMN_HEAD;

import java.util.Date;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import space.sadfox.owlook.base.moduleapi.OwlookModuleInfo;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.ui.base.FormDesigners;

class OwlTableView extends TableView<Owl<?>> {

	public final TableColumn<Owl<?>, String> ID;
	public final TableColumn<Owl<?>, String> TITLE;
	public final TableColumn<Owl<?>, String> OWL_NAME;
	public final TableColumn<Owl<?>, String> MODULE_NAME;
	public final TableColumn<Owl<?>, String> DATE_CREATED;

	public OwlTableView() {
		ID = FormDesigners.addTo(this, new TableColumn<>(ID_COLUMN_HEAD));
		ID.setCellValueFactory(cellData -> {
			return new SimpleStringProperty(cellData.getValue().fileName());
		});

		TITLE = FormDesigners.addTo(this, new TableColumn<>(TITLE_COLUMN_HEAD));
		TITLE.setCellValueFactory(cellData -> {
			return cellData.getValue().head().titleProperty();
		});

		OWL_NAME = FormDesigners.addTo(this, new TableColumn<>(OWL_NAME_COLUMN_HEAD));
		OWL_NAME.setCellValueFactory(cellData -> {
			return new SimpleStringProperty(cellData.getValue().info().owlName());
		});

		MODULE_NAME = FormDesigners.addTo(this, new TableColumn<>(MODULE_COLUMN_HEAD));
		MODULE_NAME.setCellValueFactory(cellData -> {
			Optional<OwlookModuleInfo> moduleName = ModuleLoader.INSTANCE
					.getLoadedModules(cellData.getValue().info().createdModule());
			if (moduleName.isPresent()) {
				return new SimpleStringProperty(moduleName.get().name());
			} else {
				return new SimpleStringProperty("Not Found");
			}
		});

		DATE_CREATED = FormDesigners.addTo(this, new TableColumn<>(DATE_COLUMN_HEAD));
		DATE_CREATED.setCellValueFactory(cellData -> {
			return new SimpleStringProperty(new Date(cellData.getValue().info().createdTime()).toString());
		});
	}

}
