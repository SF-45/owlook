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
import space.sadfox.owlook.moduleapi.Module;
import space.sadfox.owlook.utils.ErrorLogger;
import space.sadfox.owlook.utils.ModuleLoader;
import space.sadfox.owlook.utils.Nullable;
import space.sadfox.owlook.utils.ProjectPath;

public enum EntityLoader {

	INSTANCE;

	@FunctionalInterface
	public static interface CreateEntityListener {
		void create(JAXBEntity entity);
	}

	@FunctionalInterface
	public static interface DeleteEntityListener {
		void delete(JAXBEntity entity);
	}
	
	@FunctionalInterface
	public static interface DuplicateEntityListener<T extends JAXBEntity> {
		void duplicate(T oldEntity, T newEntity);
	}

	private final Map<Path, JAXBEntity> loaded = new HashMap<>();;
	private final List<CreateEntityListener> createListeners = new ArrayList<>();
	private final List<DeleteEntityListener> deleteListeners = new ArrayList<>();
	private final List<DuplicateEntityListener> duplicateListeners = new ArrayList<>();
	private final String extension = ".owl";

	public boolean isLoad(Path path) {
		return loaded.containsKey(path.toAbsolutePath());
	}

	public boolean entityExist(String fileName, Class<?> target) {
		Path path = validatePath(fileName, target);
		return Files.exists(path);
	}

	@SuppressWarnings("unchecked")
	public <T extends JAXBEntity> T loadEntity(String fileName, Class<T> target)
			throws IOException, JAXBEntityValidateException, JAXBException {
		Path path = validatePath(fileName, target);
		if (Files.notExists(path))
			throw new FileNotFoundException(path + " not found");
		if (!loaded.containsKey(path)) {
			T instance = new JAXBHelper<>(path, target).getInstance();
//			if (instance.getPreLoadAction() != null) {
//				instance.getPreLoadAction().action(instance);
//			} TODO: Удалить потом
			instance.validate();
			instance.initialize();
			loaded.put(path, instance);
			return instance;
		}
		return (T) loaded.get(path);
	}

	public JAXBEntity tryParseEntity(Path path) throws Nullable, FileNotFoundException, JAXBEntityValidateException {
		if (Files.notExists(path)) {
			throw new FileNotFoundException(path + " not found");
		}

		JAXBEntity instance;
		for (Module module : ModuleLoader.INSTANCE.loadModules()) {
			try {
				for (Class<? extends JAXBEntity> targetClass : module.getJaxbEntities()) {
					try {
						instance = new JAXBHelper<>(path, targetClass).getInstance();
						instance.validate();
						instance.initialize();
						return instance;
					} catch (JAXBEntityValidateException e) {
						throw e;
					} catch (FileNotFoundException | JAXBException e) {
					}
				}
			} catch (Nullable e) {
			}
		}

		throw new Nullable();
	}

	public <T extends JAXBEntity> List<T> loadAllEntities(Class<T> target) {
		List<T> entitiesList = new ArrayList<>();
		List<Path> entityPaths;

		Path path = validatePath(target);

		try {
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
		} catch (IOException e) {
			ErrorLogger.registerException(e);
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
		notifyCreateChangeListeners(instance);

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

	public <T extends JAXBEntity> T duplicateEntity(T entity) throws JAXBException, IOException {
		@SuppressWarnings("unchecked")
		T newEntity = (T) createEntity(entity.getClass());
		newEntity.syncWith(entity);
		notifyDuplicateChangeListeners(entity, newEntity);
		return newEntity;
	}

	//TODO: Добавить логирование
	public <T extends JAXBEntity> boolean deleteEntity(T entity) {
		try {
			Files.delete(entity.getPath());
			loaded.remove(entity.getPath());
			try {
				Files.find(entity.getPath().getParent(), 1, (p, basicFileAttributes) -> {
					return p.getFileName().toString().startsWith(entity.getFileName() + ".owl");
				}).forEach(p -> {
					try {
						Files.delete(p);
					} catch (IOException e) {
					}
				});
			} catch (IOException e) {
			}
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
			notifyDeleteChangeListeners(entity);
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

	private void notifyCreateChangeListeners(JAXBEntity entity) {
		createListeners.forEach(i -> i.create(entity));
	}

	public void addDeleteChangeListener(DeleteEntityListener listener) {
		deleteListeners.add(listener);
	}

	public void removeDeleteChangeListener(DeleteEntityListener listener) {
		deleteListeners.remove(listener);
	}

	private void notifyDeleteChangeListeners(JAXBEntity entity) {
		deleteListeners.forEach(i -> i.delete(entity));
	}
	
	
	
	
	
	
	
	public void addDuplicateChangeListener(DuplicateEntityListener listener) {
		duplicateListeners.add(listener);
	}

	public void removeDuplicateChangeListener(DuplicateEntityListener listener) {
		duplicateListeners.remove(listener);
	}

	private <T extends JAXBEntity> void notifyDuplicateChangeListeners(T oldEntity, T newEntity) {
		duplicateListeners.forEach(i -> i.duplicate(oldEntity, newEntity));
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
