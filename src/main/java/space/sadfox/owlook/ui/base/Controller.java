package space.sadfox.owlook.ui.base;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import space.sadfox.owlook.utils.StageFactory;

public abstract class Controller {
	private Stage rootStage;
	protected final StringProperty stageTitle = new SimpleStringProperty();
	
	public abstract Parent getParent();

	public final Stage getStage() {
		if (rootStage == null) {
			rootStage = StageFactory.INSTANCE.createStage();
			rootStage.setScene(new Scene(getParent()));
			rootStage.titleProperty().bind(stageTitle);
		}
		return rootStage;
	}
	
	public final void setModality(Modality modality) {
		modality = modality == null ? Modality.APPLICATION_MODAL: modality;
		getStage().initModality(modality);
		getStage().initOwner(StageFactory.INSTANCE.getCurrentStage());
	}
	
	public final void show() {
		if (!getStage().getModality().equals(Modality.NONE)) {
			getStage().initModality(Modality.NONE);
		}
		getStage().show();
	}
	
	public final void showAndWait() {
		getStage().showAndWait();
	}
	
	public final String getStageTitle() {
		return stageTitle.get();
	}
	
	public final ReadOnlyStringProperty stageTitleProperty() {
		return stageTitle;
	}

}
