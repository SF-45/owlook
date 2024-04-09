package space.sadfox.owlook.ui.tools;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import space.sadfox.owlook.utils.MessageLevel;
import space.sadfox.owlook.utils.OwlookMessage;

public class MessageBox extends Alert {

  public MessageBox(AlertType alertType) {
    super(alertType);
  }

  public MessageBox(AlertType alertType, String contentText, ButtonType... buttons) {
    super(alertType, contentText, buttons);

  }

  public MessageBox(OwlookMessage message) {
    super(messageLevelConverter(message.getMessageLevel()));
    setHeaderText(message.getName());
    setTitle(message.getName());
    setContentText(message.getMessage());
  }

  private static AlertType messageLevelConverter(MessageLevel level) {
    AlertType type = null;
    switch (level) {
      case WARNING:
        type = AlertType.WARNING;
        break;
      case INFO:
        type = AlertType.INFORMATION;
        break;
      case ERROR:
        type = AlertType.ERROR;
        break;
      case DEBUG:
        type = AlertType.ERROR;
        break;
      default:
        type = AlertType.NONE;
    }
    return type;

  }
}
