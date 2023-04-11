package space.sadfox.owlook;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import space.sadfox.owlook.ui.MainStage;
import space.sadfox.owlook.utils.ModuleLoader;
import space.sadfox.owlook.utils.StageFactory;
import space.sadfox.owlook.moduleapi.Module;

public class App extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		StageFactory.INSTANCE.registerStage(primaryStage);
		
		MainStage mainStage = new MainStage(ModuleLoader.INSTANCE.loadModules(Module.class));
		primaryStage.setScene(new Scene(mainStage.getParent()));
		primaryStage.show();
		
	}
	
	public static void go() {
		launch();
	}

}
