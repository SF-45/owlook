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
import space.sadfox.owlook.jaxb.EntityChangeListener.Change;
import space.sadfox.owlook.utils.ErrorLogger;
import space.sadfox.owlook.utils.ProjectPath;

public class EntityLoader {
	
	@FunctionalInterface
	public static interface CreateEntityListener {
		void create(JAXBEntity entity);
	}

	private static Map<Path, JAXBEntity> loaded;
	private static List<CreateEntityListener> createListeners;
	private static final String extension = ".owl";

	static {
		loaded = new HashMap<>();
		createListeners = new ArrayList<>();
	}

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
//			if (instance.getPreLoadAction() != null) {
//				instance.getPreLoadAction().action(instance);
//			} TODO: Удалить потом
			if (!instance.validate()) {
				throw new JAXBException("An exception occurred during initialization. See log for details");
			}
			instance.initialize();
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
		}).collect(Collectors.toList());
		for (Path entityPath : entityPaths) {
			try {
				entitiesList.add(loadEntity(entityPath.getFileName().toString(), target));
			} catch (IOException | JAXBException e) {
				ErrorLogger.registerException(e);
			}
		}

		return entitiesList;

	}

	private <T extends JAXBEntity> T createEntity(String fileName, Class<T> target) throws JAXBException, IOException {
		Path path = validatePath(target);

		if (Files.isDirectory(path) && Files.notExists(path)) {
			Files.createDirectories(path);
		}

		path = validatePath(fileName, target);

		if (Files.exists(path))
			throw new JAXBException("[" + path + "] alredy exist");
		T instance = new JAXBHelper<>(path, target).getInstance();
		loaded.put(path, instance);
		instance.saveImmediately();
		notifyEntityChangeListeners(instance);

		return instance;
	}

	public <T extends JAXBEntity> T createEntity(Class<T> target) throws JAXBException, IOException {
		return createEntity(generateFileName(), target);
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
			entity.notifyEntityChangeListeners(new Change() {
				
				@Override
				public boolean wasRemoved() {
					return true;
				}
				
				@Override
				public boolean wasModify() {
					return false;
				}
			});
			return true;
		} catch (IOException e) {
			ErrorLogger.registerException(e);
		}
		return false;
	}

	public void addCreateChangeListener(CreateEntityListener listener) {
		createListeners.add(listener);
	}

	public void removeCreateChangeListener(CreateEntityListener listener) {
		createListeners.remove(listener);
	}

	private void notifyEntityChangeListeners(JAXBEntity entity) {
		createListeners.forEach(i -> i.create(entity));
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

	private String generateFileName() {
		return String.valueOf(System.currentTimeMillis());
	}

}
