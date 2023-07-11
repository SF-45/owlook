package space.sadfox.owlook;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import space.sadfox.owlook.ui.MainStage;
import space.sadfox.owlook.utils.StageFactory;

public class App extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		StageFactory.INSTANCE.registerStage(primaryStage);
		
		MainStage mainStage = new MainStage();
		primaryStage.setScene(new Scene(mainStage.getParent()));
		primaryStage.setTitle("Owlook");
		primaryStage.show();
		
	}
	
	public static void go() {
		launch();
	}

}
