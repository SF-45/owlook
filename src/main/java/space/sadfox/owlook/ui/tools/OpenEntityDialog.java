package space.sadfox.owlook.ui.tools;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import space.sadfox.owlook.ResourceTarget;
import space.sadfox.owlook.base.jaxb.JAXBEntity;
import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.utils.EntityLoader;

public class OpenEntityDialog<T extends JAXBEntity> extends Controller {

	@FXML
	private Button open;

	@FXML
	private TableView<T> tableView;

	@FXML
	private TextArea previewArea;

	private boolean isOpened = false;

	public OpenEntityDialog(Class<T> target, SelectionMode selectionMode, List<T> alredyOpenned) throws IOException {
		super(ResourceTarget.class.getResource("fxml/open-entity-dialog.fxml"));
		tableView.getSelectionModel().setSelectionMode(selectionMode);
		init();
		
		ObservableList<T> allEntities = FXCollections.observableList(EntityLoader.INSTANCE.loadAllEntities(target));

		allEntities = allEntities.filtered(f -> {
			for (var e : alredyOpenned) {
				if (f.equals(e))
					return false;
			}
			return true;
		});

		tableView.setItems(FXCollections.observableList(allEntities));
	}

	public OpenEntityDialog(Class<T> target, SelectionMode selectionMode) throws IOException {
		super(ResourceTarget.class.getResource("fxml/open-entity-dialog.fxml"));
		tableView.getSelectionModel().setSelectionMode(selectionMode);
		
		init();
		ObservableList<T> allEntities = FXCollections.observableList(EntityLoader.INSTANCE.loadAllEntities(target));

		tableView.setItems(FXCollections.observableList(allEntities));
	}

	private void init() {

		open.setOnAction(event -> {
			openAction();
		});
		tableView.getSelectionModel().selectedItemProperty().addListener(prop -> {
			open.setDisable(tableView.getSelectionModel().isEmpty());
		});

		initTableView();
		initKeyBind();
	}

	private void initTableView() {
		TableColumn<T, String> fileName = new TableColumn<>("ID");
		fileName.setCellValueFactory(call -> {
			return new SimpleStringProperty(call.getValue().getFileName());
		});
		tableView.getColumns().add(fileName);

		TableColumn<T, String> title = new TableColumn<>("Title");
		title.setCellValueFactory(new PropertyValueFactory<>("title"));
		tableView.getColumns().add(title);

		tableView.setRowFactory(callback -> {
			TableRow<T> row = new TableRow<>();
			row.setOnMouseClicked(mouseEvent -> {
				switch (mouseEvent.getButton()) {
				case PRIMARY:
					if (mouseEvent.getClickCount() == 2) {
						openAction();
					}
					break;

				default:
					break;
				}
			});
			return row;
		});

		TableViewSelectionModel<T> selectionModel = tableView.getSelectionModel();
		selectionModel.selectedItemProperty().addListener((property, oldValue, newValue) -> {
			previewArea.setText(newValue.toString());
		});
	}

	private void initKeyBind() {
		tableView.setOnKeyPressed(keyEvent -> {
			switch (keyEvent.getCode()) {
			case ENTER:
				if (tableView.getSelectionModel().isEmpty())
					break;
				openAction();
				break;
			case A:
				if (keyEvent.isControlDown()) {
					tableView.getSelectionModel().selectAll();
				}
				break;

			default:
				break;
			}
		});
	}

	private void openAction() {
		isOpened = true;
		getStage().close();
	}

	public List<T> getOpenned() {
		return tableView.getSelectionModel().getSelectedItems();
	}

	public boolean isOpened() {
		return isOpened;
	}

}
