package space.sadfox.owlook.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import space.sadfox.owlook.ui.base.FormDesigner;
import space.sadfox.owlook.ui.base.FormDesigners;

public class MainStageDesigner extends FormDesigner {

  final VBox root = new VBox();
  final WSItemList wsItemList;
  final Menu owleryMenuButton;
  final Menu file;
  final Menu edit;

  MainStageDesigner() {
    // MENU_BAR
    HBox menuBarHBox = FormDesigners.addTo(root, new HBox());
    FormDesigners.bindMinWidth(root, menuBarHBox);

    MenuBar menuBar = FormDesigners.addTo(menuBarHBox, new MenuBar());
    FormDesigners.bindMinWidth(menuBarHBox, menuBar);

    file = FormDesigners.addTo(menuBar, new Menu("File"));
    edit = FormDesigners.addTo(menuBar, new Menu("Edit"));

    owleryMenuButton = FormDesigners.addTo(menuBar, new Menu("Owlery"));
    MenuItem hiddenItem = FormDesigners.addTo(owleryMenuButton, new MenuItem());
    owleryMenuButton.setOnShown(event -> owleryMenuButton.hide());



    // BODY
    wsItemList = FormDesigners.addTo(root, new WSItemList());
    VBox.setMargin(wsItemList, new Insets(5d));

  }

  @Override
  protected Parent root() {
    return root;
  }


}
