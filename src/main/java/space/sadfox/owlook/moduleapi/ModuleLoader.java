package space.sadfox.owlook.moduleapi;

import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum ModuleLoader {
	INSTANCE;

	private ModuleLayer moduleLayer;	

	public <T> List<T> loadModules(Class<T> target) {
		return ServiceLoader.load(moduleLayer, target).stream().map(Provider::get).collect(Collectors.toList());
	}
	
	void setModuleLayer(ModuleLayer moduleLayer) {
		this.moduleLayer = moduleLayer;
	}

	public List<OwlookModule> loadModules() {
		return loadModules(OwlookModule.class);
	}

	public List<Workspace> loadWorkspaces() {
		return loadModules(Workspace.class);
	}

	public List<ModuleExtension> loadModuleExtension() {
		return loadModules(ModuleExtension.class);
	}

	@SuppressWarnings("unchecked")
	public <T extends ModuleExtension> List<T> loadModuleExtension(Class<T> target,
			Predicate<? super ModuleExtension> predicate) {

		return loadModules(ModuleExtension.class).stream().filter(predicate).map(m -> (T) m).collect(Collectors.toList());
	}

}
