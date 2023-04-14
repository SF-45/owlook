package space.sadfox.owlook.moduleapi;

import java.util.List;

public interface Module {
	String getModuleName();
	String getModuleDescription();
	String getModuleVersion();
	default List<Service> getServices() {
		return null;
	}
	default List<Utility> getUtilities() {
		return null;
	}
	default List<Workspace> getWorkspaces() {
		return null;
	}
}
