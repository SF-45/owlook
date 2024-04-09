package space.sadfox.owlook;

import java.io.IOException;
import org.apache.commons.io.FileUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import space.sadfox.owlook.moduleloader.ModuleLoaderController;
import space.sadfox.owlook.utils.Owlook;
import space.sadfox.owlook.utils.OwlookConfiguration;
import space.sadfox.owlook.utils.ProjectPath;
import space.sadfox.owlook.utils.StageFactory;

public class App extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    StageFactory.INSTANCE.registerStage(primaryStage);

    ModuleLoaderController moduleLoaderController = new ModuleLoaderController();

    OwlookConfiguration config = Owlook.getConfig();
    if (!config.isSkipModuleManage() || !moduleLoaderController.launch()) {
      primaryStage.setScene(new Scene(moduleLoaderController.getParent()));
      primaryStage.setTitle(moduleLoaderController.getStageTitle());
      primaryStage.show();
    }

  }

  @Override
  public void stop() throws IOException {
    FileUtils.deleteQuietly(ProjectPath.TEMP.getPath().toFile());
  }

  public static void go() {
    launch();
  }

}
