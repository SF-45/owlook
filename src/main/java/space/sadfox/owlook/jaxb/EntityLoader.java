package space.sadfox.owlook.jaxb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.utils.ErrorLogger;
import space.sadfox.owlook.utils.ProjectPath;

public class EntityLoader {

	private static Map<Path, JAXBEntity> loaded = new HashMap<>();
	private static final String extension = ".owl";

	public boolean isLoad(Path path) {
		return loaded.containsKey(path.toAbsolutePath());
	}

	@SuppressWarnings("unchecked")
	public <T extends JAXBEntity> T loadEntity(String fileName, Class<T> target) throws IOException, JAXBException {
		Path path = validatePath(fileName, target);
		if (Files.notExists(path))
			throw new FileNotFoundException(path + " not found");
		if (!loaded.containsKey(path)) {
			T instance = new JAXBHelper<>(path, target).getInstance();
			if (instance.getPreLoadAction() != null) {
				instance.getPreLoadAction().action(instance);
			}
			loaded.put(path, instance);
			return instance;
		}
		return (T) loaded.get(path);
	}

	public <T extends JAXBEntity> List<T> loadAllEntities(Class<T> target) throws IOException {
		List<T> entitiesList = new ArrayList<>();
		List<Path> entityPaths;

		Path path = validatePath(target);
		entityPaths = Files.find(path, 1, (p, basicFileAttributes) -> {
			return p.getFileName().toString().endsWith(extension);
		})
				.collect(Collectors.toList());
		for (Path entityPath : entityPaths) {
			try {
				entitiesList.add(loadEntity(entityPath.getFileName().toString(), target));
			} catch (IOException | JAXBException e) {
				ErrorLogger.registerException(e);
			}
		}

		return entitiesList;

	}

	public <T extends JAXBEntity> T createEntity(String fileName, Class<T> target) throws JAXBException, IOException {
		Path path = validatePath(target);

		if (Files.isDirectory(path) && Files.notExists(path)) {
			Files.createDirectories(path);
		}

		path = validatePath(fileName, target);

		if (Files.exists(path))
			throw new JAXBException(path + "] alredy exist");
		T instance = new JAXBHelper<>(path, target).getInstance();
		loaded.put(path, instance);
		instance.saveImmediately();
		return instance;
	}

	public <T extends JAXBEntity> T createOrLoadEntity(String fileName, Class<T> target)
			throws JAXBException, IOException {
		Path path = validatePath(fileName, target);
		T instance;
		if (Files.exists(path)) {
			instance = loadEntity(fileName, target);
		} else {
			instance = createEntity(fileName, target);
		}
		return instance;
	}
	
	public boolean entityExist(String fileName, Class<?> target) {
		Path path = validatePath(fileName, target);
		return Files.exists(path);
	}
	
	public <T extends JAXBEntity> boolean deleteEntity(T entity) {
		try {
			Files.delete(entity.getPath());
			loaded.remove(entity.getPath());
			return true;
		} catch (IOException e) {
			ErrorLogger.registerException(e);
		}
		return false;
	}
	
	private Path validatePath(String fileName, Class<?> target) {
		Path path = ProjectPath.CONFiG.getPath().resolve(target.getPackageName()).toAbsolutePath();
		if (!fileName.endsWith(extension)) {
			return path.resolve(fileName + extension);
		} else {
			return path.resolve(fileName);
		}
	}
	
	private Path validatePath(Class<?> target) {
		Path path = ProjectPath.CONFiG.getPath().resolve(target.getPackageName()).toAbsolutePath();
		
		return path;
	}

}
