package space.sadfox.owlook.moduleapi;

import java.util.List;

import space.sadfox.owlook.jaxb.JAXBEntity;
import space.sadfox.owlook.utils.Nullable;

public interface Module {
	String getModuleName();
	String getModuleDescription();
	String getModuleVersion();
	List<Class<? extends JAXBEntity>> getJaxbEntities() throws Nullable;
	
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
