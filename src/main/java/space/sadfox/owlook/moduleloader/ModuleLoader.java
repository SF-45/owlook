package space.sadfox.owlook.moduleloader;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.FindException;
import java.lang.module.ModuleFinder;
import java.lang.module.ResolutionException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import space.sadfox.owlook.OwlookConfiguration;
import space.sadfox.owlook.base.moduleapi.OwlookModule;
import space.sadfox.owlook.base.moduleapi.OwlookModuleComponent;
import space.sadfox.owlook.base.moduleapi.OwlookModulePack;
import space.sadfox.owlook.base.moduleapi.VersionFormat;
import space.sadfox.owlook.component.Workspace;
import space.sadfox.owlook.utils.OwlLogger;
import space.sadfox.owlook.utils.ProjectPath;

public enum ModuleLoader {
	INSTANCE;

	class LoadReport {
		private final List<ModuleLoadInfo> loadInfoList = new ArrayList<>();
		private boolean load = true;

		private ModuleLayer resultLayer;

		private LoadReport() {
		}

		public List<ModuleLoadInfo> getModuleLoadInfoList() {
			return Collections.unmodifiableList(loadInfoList);
		}

		public boolean isLoad() {
			return load;
		}
	}

	private final Map<Path, OwlookModulePack> owlookModulePacks = new HashMap<>();

	private ModuleLayer moduleLayer;
	private boolean initModules = false;

	public <T> List<T> loadModules(Class<T> target) {
		return ServiceLoader.load(moduleLayer, target).stream().map(Provider::get).collect(Collectors.toList());
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

		return loadModules(OwlookModuleComponent.class).stream().filter(predicate).map(m -> (T) m)
				.collect(Collectors.toList());
	}

	Collection<OwlookModulePack> loadedModulePacks() throws IOException {
		return Collections.unmodifiableCollection(owlookModulePacks.values());
	}

	OwlookModulePack getModulePack(Path moduleFile) throws IOException {
		moduleFile = moduleFile.toAbsolutePath();
		if (!owlookModulePacks.containsKey(moduleFile)) {
			OwlookModulePack pack = new OwlookModulePack(moduleFile);
			owlookModulePacks.put(moduleFile, pack);
		}
		return owlookModulePacks.get(moduleFile);
	}

	List<OwlookModulePack> getModulePacks(Path... moduleFiles) {
		return getModulePacks(Arrays.asList(moduleFiles));
	}

	List<OwlookModulePack> getModulePacks(Collection<Path> moduleFiles) {
		List<OwlookModulePack> returnList = new ArrayList<>();
		for (Path moduleFile : moduleFiles) {
			try {
				returnList.add(getModulePack(moduleFile));
			} catch (IOException e) {
				OwlLogger.registerException(2, e);
			}
		}
		return returnList;
	}

	void removeModulePack(OwlookModulePack owlookModulePack) throws IOException {
		if (owlookModulePacks.containsKey(owlookModulePack.LOCATION)) {
			owlookModulePack.close();
			owlookModulePacks.remove(owlookModulePack.LOCATION);
		}

		Files.deleteIfExists(owlookModulePack.LOCATION);
	}

	LoadReport testBoot(Collection<OwlookModulePack> modulePacks) {

		List<OwlookModulePack> pool = new ArrayList<>(modulePacks);

		LoadReport loadReport = new LoadReport();

		ModuleLayer moduleLayer = ModuleLayer.boot();

		VersionFormat projectVersion = OwlookConfiguration.instance().getVersion();

		List<OwlookModulePack> testVersions = new ArrayList<>(pool);
		testVersions.forEach(pack -> {
			if (!pack.MODILE_INFO.version().compatibleWith(projectVersion)) {
				pool.remove(pack);

				String errorMassage = "Module version is incompatible with project version. " + pack.MODILE_INFO.name()
						+ "-" + pack.MODILE_INFO.version() + " incompatible with Owlook-" + projectVersion;

				loadReport.loadInfoList.add(new ModuleLoadInfo(pack, ModuleLoadInfo.Status.ERROR, errorMassage));
			}
		});

		Map<String, List<OwlookModulePack>> loadedModules = new HashMap<>();

		pool.forEach(pack -> {
			if (loadedModules.containsKey(pack.MODILE_INFO.moduleName())) {
				loadedModules.get(pack.MODILE_INFO.moduleName()).add(pack);
			} else {
				loadedModules.put(pack.MODILE_INFO.moduleName(), new ArrayList<>(Arrays.asList(pack)));
			}
		});

		loadedModules.forEach((key, value) -> {
			if (value.size() > 1) {
				value.forEach(pack -> {
					if (pool.remove(pack)) {
						StringBuilder builder = new StringBuilder("Duplicate module:");
						value.forEach(pack2 -> {
							builder.append("\n " + pack2.MODILE_INFO.name() + "-" + pack2.MODILE_INFO.version() + " ["
									+ pack2.LOCATION + "]");
						});
						loadReport.loadInfoList
								.add(new ModuleLoadInfo(pack, ModuleLoadInfo.Status.ERROR, builder.toString()));
					}
				});
				loadReport.load = false;
			}
		});

		LoadOrder loadOrder = new LoadOrder(pool);

		OwlookModulePack loadPack = null;
		while ((loadPack = loadOrder.poll()) != null) {
			ModuleFinder moduleFinder = ModuleFinder.of(loadPack.MODULE_PATH);
			ModuleFinder libFinder = ModuleFinder.of(loadPack.LIB_PATH);

			try {
				Configuration configuration = moduleLayer.configuration().resolve(moduleFinder, libFinder,
						Arrays.asList(loadPack.MODILE_INFO.moduleName()));

				moduleLayer = moduleLayer.defineModulesWithOneLoader(configuration, ClassLoader.getSystemClassLoader());
				loadReport.loadInfoList.add(new ModuleLoadInfo(loadPack, ModuleLoadInfo.Status.OK, ""));
			} catch (FindException | ResolutionException e) {
				OwlLogger.registerException(3, e);
				loadReport.loadInfoList.add(new ModuleLoadInfo(loadPack, ModuleLoadInfo.Status.ERROR, e.getMessage()));
				loadReport.load = false;
			}

		}

		if (loadReport.isLoad()) {
			loadReport.resultLayer = moduleLayer;
		}

		return loadReport;

	}

	LoadReport boot(Collection<OwlookModulePack> modulePacks) {
		LoadReport loadReport = testBoot(modulePacks);
		if (loadReport.isLoad()) {
			this.moduleLayer = loadReport.resultLayer;
		}
		return loadReport;

	}

	void initOwlookModules(boolean forceInit) {
		if (!isBoot())
			return;
		if (!isInitModules() || forceInit) {
			loadModules().forEach(OwlookModule::initModule);
		}
	}

	boolean isBoot() {
		return moduleLayer != null;
	}

	boolean isInitModules() {
		return initModules;
	}

	static List<Path> findModuleFiles() throws IOException {
		return Files.find(ProjectPath.MODULE.getPath(), 1, (p, attr) -> p.getFileName().toString().endsWith(".owlm"))
				.map(Path::toAbsolutePath).collect(Collectors.toList());
	}

}
