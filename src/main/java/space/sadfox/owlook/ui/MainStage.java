package space.sadfox.owlook.ui;

import java.io.IOException;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.owlery.OwlLoader;
import space.sadfox.owlook.owlery.Owlery;
import space.sadfox.owlook.ui.base.DesignController;

public class MainStage extends DesignController<MainStageDesigner> {

  private Owlery owlery;

  public MainStage() throws IOException {
    super(new MainStageDesigner());
    stageTitle.set("Owlook");

    OwlLoader.INSTANCE.boot();

    DESIGN.owleryMenuButton.setOnShowing(event -> showOwlery());
    DESIGN.wsItemList.getItems().addAll(ModuleLoader.INSTANCE.loadWorkspaces());
  }

  private void showOwlery() {
    if (owlery == null) {
      owlery = new Owlery();
    }
    owlery.show();
  }
}
