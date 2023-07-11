package space.sadfox.owlook.ui.tools;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class MessageBox extends Alert {

	public MessageBox(AlertType alertType) {
		super(alertType);
	}

	public MessageBox(AlertType alertType, String contentText, ButtonType... buttons) {
		super(alertType, contentText, buttons);
		
	}
}
