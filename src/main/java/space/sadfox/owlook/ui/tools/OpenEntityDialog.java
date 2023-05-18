package space.sadfox.owlook.ui.tools;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import space.sadfox.owlook.ResourceTarget;
import space.sadfox.owlook.jaxb.EntityLoader;
import space.sadfox.owlook.jaxb.JAXBEntity;
import space.sadfox.owlook.ui.base.Controller;

public class OpenEntityDialog<T extends JAXBEntity> extends Controller {

	@FXML
	private Button open;

	@FXML
	private TableView<T> tableView;
	
	@FXML
    private TextArea previewArea;
	
	private boolean isOpened = false;

	public OpenEntityDialog(Class<T> target, List<T> alredyOpenned) throws IOException {
		super(ResourceTarget.class.getResource("fxml/open-entity-dialog.fxml"));
		
		TableColumn<T, String> fileName = new TableColumn<>("ID");
		fileName.setCellValueFactory(call -> {
			return new SimpleStringProperty(call.getValue().getFileName());
		});
		
		TableColumn<T, String> title = new TableColumn<>("Title");
		title.setCellValueFactory(new PropertyValueFactory<>("title"));
		
		tableView.getColumns().addAll(fileName, title);
		
		tableView.getSelectionModel().selectedItemProperty().addListener((property, oldValue, newValue) -> {
			previewArea.setText(newValue.toString());
		});
		
		ObservableList<T> allEntities = FXCollections.observableList(EntityLoader.INSTANCE.loadAllEntities(target));
		
		if (alredyOpenned != null) {
			allEntities = allEntities.filtered(f -> {
				for (var e : alredyOpenned) {
					if (f.equals(e)) return false;
				}
				return true;
			});
		}
		
		tableView.setItems(FXCollections.observableList(allEntities));
		
		
		tableView.getSelectionModel().selectedIndexProperty().addListener((prop, oldVal, newVal) -> {
			if (newVal.intValue() < 0) open.setDisable(true);
			else open.setDisable(false);
		});
		open.setOnAction(event -> {
			isOpened = true;
			getStage().close();
		});
		
		
		
	}
	
	public T getOpenned() {
		return tableView.getSelectionModel().getSelectedItem();
	}
	
	public boolean isOpened() {
		return isOpened;
	}

}
