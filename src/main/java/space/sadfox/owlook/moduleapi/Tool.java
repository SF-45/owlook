package space.sadfox.owlook.moduleapi;

import space.sadfox.owlook.ui.base.Controller;

public interface Tool {
	String getToolName();
	String getToolDescription();
	Controller getConfigController();

}
