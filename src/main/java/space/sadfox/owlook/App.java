package space.sadfox.owlook;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import space.sadfox.owlook.configuration.OwlookConfigurationEntity;
import space.sadfox.owlook.moduleapi.ModuleLoaderController;
import space.sadfox.owlook.ui.MainStage;
import space.sadfox.owlook.utils.StageFactory;

public class App extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		StageFactory.INSTANCE.registerStage(primaryStage);
		
		ModuleLoaderController moduleLoaderController = new ModuleLoaderController();
		
		OwlookConfigurationEntity config = OwlookModuleProvider.getConfig();
		
		if (!config.isSkipModuleManage() || !moduleLoaderController.launch()) {
			primaryStage.setScene(new Scene(moduleLoaderController.getParent()));
			primaryStage.setTitle(moduleLoaderController.getStageTitle());
			primaryStage.show();
		}
		
		
	}
	
	public static void go() {
		launch();
	}

}
