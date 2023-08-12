package space.sadfox.owlook.ui.base;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class FXMLController extends Controller {
	private final Parent root;
	
	public FXMLController(URL resource) throws IOException {
		FXMLLoader loader = new FXMLLoader(resource);
		loader.setController(this);
		root = loader.load();
	}

	@Override
	public final Parent getParent() {
		return root;
	}
}
