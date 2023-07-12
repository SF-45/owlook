package space.sadfox.owlook.ui.base;

import java.io.IOException;
import java.net.URL;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import space.sadfox.owlook.utils.StageFactory;

public abstract class Controller {
	private Parent root;
	private Stage rootStage;
	protected final StringProperty stageTitle = new SimpleStringProperty();

	public Controller(URL resource) throws IOException {
		FXMLLoader loader = new FXMLLoader(resource);
		loader.setController(this);
		root = loader.load();
	}
	
	public Parent getParent() {
		return root;
	}

	public Stage getStage() {
		if (rootStage == null) {
			rootStage = StageFactory.INSTANCE.createStage();
			rootStage.setScene(new Scene(getParent()));
			rootStage.titleProperty().bind(stageTitle);
		}
		return rootStage;
	}
	
	public void setModality(Modality modality) {
		modality = modality == null ? Modality.APPLICATION_MODAL: modality;
		getStage().initModality(modality);
		getStage().initOwner(StageFactory.INSTANCE.getCurrentStage());
	}
	
	public void show() {
		if (!getStage().getModality().equals(Modality.NONE)) {
			getStage().initModality(Modality.NONE);
		}
		getStage().show();
	}
	
	public void showAndWait() {
		getStage().showAndWait();
	}
	
	public String getStageTitle() {
		return stageTitle.get();
	}
	
	public ReadOnlyStringProperty stageTitleProperty() {
		return stageTitle;
	}

}
