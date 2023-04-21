package space.sadfox.owlook.moduleapi;

import java.util.List;

public interface Module {
	String getModuleName();
	String getModuleDescription();
	String getModuleVersion();
	default List<Tool> getTools() {
		return null;
	}
	default List<ModuleExtension> getUtilities() {
		return null;
	}
	default List<Workspace> getWorkspaces() {
		return null;
	}
}
