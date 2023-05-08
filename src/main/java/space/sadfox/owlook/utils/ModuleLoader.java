package space.sadfox.owlook.utils;

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import space.sadfox.owlook.moduleapi.ModuleExtension;
import space.sadfox.owlook.moduleapi.Workspace;
import space.sadfox.owlook.moduleapi.Module;

public enum ModuleLoader {
	INSTANCE;

	private ModuleLayer moduleLayer;

	public void updateModuleList() {
		// Будем искать плагины в папке plugins
		ModuleFinder pluginsFinder = ModuleFinder.of(ProjectPath.MODULE.getPath());
		ModuleFinder libFinder = ModuleFinder.of(ProjectPath.MODULE_LIB.getPath());
		// ModuleFinder pluginsFinder = ModuleFinder.of(ProjectPath.MODULE.getPath(),
		// ProjectPath.MODULE_LIB.getPath());
		// ModuleFinder pluginsFinder = ModuleFinder.ofSystem();
		// Пусть ModuleFinder найдёт все модули в папке plugins и вернёт нам список их
		// имён
		List<String> plugins = pluginsFinder.findAll().stream().map(ModuleReference::descriptor)
				.map(ModuleDescriptor::name).collect(Collectors.toList());

//		plugins.addAll(libFinder.findAll().stream().map(ModuleReference::descriptor)
//				.map(ModuleDescriptor::name).collect(Collectors.toList()));

		// Создадим конфигурацию, которая выполнит резолюцию указанных модулей (проверит
		// корректность графа зависимостей)
		Configuration pluginsConfiguration = ModuleLayer.boot().configuration().resolve(pluginsFinder, libFinder,
				plugins);

		// Создадим слой модулей для плагинов
		moduleLayer = ModuleLayer.boot().defineModulesWithOneLoader(pluginsConfiguration,
				ClassLoader.getSystemClassLoader());
	}

	public <T> List<T> loadModules(Class<T> target) {
		if (moduleLayer == null)
			updateModuleList();

		return ServiceLoader.load(moduleLayer, target).stream().map(Provider::get).collect(Collectors.toList());
	}

	public List<Module> loadModules() {
		return loadModules(Module.class);
	}

	public List<Workspace> loadWorkspaces() {
		return loadModules(Workspace.class);
	}

	public List<ModuleExtension> loadModuleExtension() {
		return loadModules(ModuleExtension.class);
	}

	public <T extends ModuleExtension> List<T> loadModuleExtension(Class<T> target,
			Predicate<? super ModuleExtension> predicate) {

		return loadModules(ModuleExtension.class).stream().filter(predicate).map(m -> (T) m).collect(Collectors.toList());
	}

}
