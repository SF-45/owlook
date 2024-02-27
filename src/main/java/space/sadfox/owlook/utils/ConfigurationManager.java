package space.sadfox.owlook.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.base.jaxb.JAXBEntityFactory;
import space.sadfox.owlook.base.jaxb.ObservedJAXBEntity;
import space.sadfox.owlook.base.moduleapi.OwlookModule;

public class ConfigurationManager<T extends ObservedJAXBEntity> {
	public static final String EXTENSION = ".conf";

	private static final Map<Path, ObservedJAXBEntity> configs = new HashMap<>();

	private final Class<T> target;

	public ConfigurationManager(Class<T> target) {
		this.target = target;
	}

	public T getConfig(Path path) throws JAXBException, IOException, ReflectiveOperationException, ClassCastException {
		path = path.toAbsolutePath();

		if (configs.containsKey(path)) {
			if (Files.notExists(path)) {
				configs.remove(path);
				throw new FileNotFoundException("File not found " + path);
			}
			return (T) configs.get(path);
		} else {
			T newInstance = new JAXBEntityFactory<>(target).instanceOf(path);
			newInstance.enableAutoSave(exception -> {
				Owlook.registerException(0, exception);
			});
			configs.put(path, newInstance);
			return newInstance;
		}
	}
	
	public T getConfig(String fileName)
			throws JAXBException, IOException, ReflectiveOperationException, ClassCastException {
		if (!fileName.endsWith(EXTENSION)) {
			fileName = fileName + EXTENSION;
		}
		Path configPath = ProjectPath.CONFiG.getPath().resolve(fileName);
		return getConfig(configPath);
	}

	public T getConfig(OwlookModule module)
			throws JAXBException, IOException, ReflectiveOperationException, ClassCastException {
		Path moduleConfigPath = ProjectPath.CONFiG.getPath()
				.resolve(module.getClass().getModule().getName() + EXTENSION);
		return getConfig(moduleConfigPath);
	}

}
