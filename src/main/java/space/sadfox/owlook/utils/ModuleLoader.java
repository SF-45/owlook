package space.sadfox.owlook.utils;

import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import space.sadfox.owlook.base.moduleapi.OwlookModule;
import space.sadfox.owlook.base.moduleapi.OwlookModuleComponent;
import space.sadfox.owlook.component.Workspace;
import space.sadfox.owlook.ui.ModuleLoaderUILink;

public enum ModuleLoader {
	INSTANCE;
	
	private ModuleLayer moduleLayer() {
		return ModuleLoaderUILink.getModuleLayer();
	}

	public <T> List<T> loadModules(Class<T> target) {
		return ServiceLoader.load(moduleLayer(), target).stream().map(Provider::get).collect(Collectors.toList());
	}

	public List<OwlookModule> loadModules() {
		return loadModules(OwlookModule.class);
	}

	public List<Workspace> loadWorkspaces() {
		return loadModuleComponents(Workspace.class, comp -> comp instanceof Workspace);
	}

	public List<OwlookModuleComponent> loadModuleComponents() {
		return loadModules(OwlookModuleComponent.class);
	}

	@SuppressWarnings("unchecked")
	public <T extends OwlookModuleComponent> List<T> loadModuleComponents(Class<T> target,
			Predicate<? super OwlookModuleComponent> predicate) {

		return loadModules(OwlookModuleComponent.class).stream().filter(predicate).map(m -> (T) m).collect(Collectors.toList());
	}

}
