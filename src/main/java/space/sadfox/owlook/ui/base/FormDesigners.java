package space.sadfox.owlook.ui.base;

import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.Pane;

public class FormDesigners {
	public static <T extends Node> T addTo(Pane pane, T node) {
		pane.getChildren().add(node);
		return node;
	}
	
	public static <T extends Node> T addTo(ButtonBar buttonBar, T node) {
		buttonBar.getButtons().add(node);
		return node;
	}

}
