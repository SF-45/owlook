package space.sadfox.owlook.ui.base;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class FXMLController extends Controller {
  private final Parent root;

  public FXMLController(URL resource) throws FXMLControllerException {
    FXMLLoader loader = new FXMLLoader(resource);
    loader.setController(this);
    try {
      root = loader.load();
    } catch (IOException e) {
      throw new FXMLControllerException("Error reading the FXML file", e);
    }
  }

  @Override
  public final Parent getParent() {
    return root;
  }
}
