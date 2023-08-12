package space.sadfox.owlook.ui.base;

import javafx.scene.Parent;

public class DesignController<T extends FormDesigner> extends Controller {
	private final Parent root;
	protected final T DESIGN;
	
	public DesignController(T formDesigner) {
		this.DESIGN = formDesigner;
		root = formDesigner.root();
		formDesigner.initialization();
	}

	@Override
	public final Parent getParent() {
		return root;
	}
}
