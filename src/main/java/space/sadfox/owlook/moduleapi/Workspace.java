package space.sadfox.owlook.moduleapi;

import space.sadfox.owlook.ui.base.Controller;

public interface Workspace {
	public abstract String getWorkspaceName();
	public abstract String getWorkspaceDescription();
	Controller getController();

}
