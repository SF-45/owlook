package space.sadfox.owlook.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.base.jaxb.EntityChangeListener.Change;
import space.sadfox.owlook.base.jaxb.JAXBEntity;
import space.sadfox.owlook.base.jaxb.JAXBEntityValidateException;
import space.sadfox.owlook.base.jaxb.JAXBHelper;
import space.sadfox.owlook.base.moduleapi.ModuleHasNoConfiguration;
import space.sadfox.owlook.base.moduleapi.ModuleHasNoProvideEntities;
import space.sadfox.owlook.base.moduleapi.OwlookModule;
import space.sadfox.owlook.logger.LogLevel;

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
	public static interface DuplicateEntityListener {
		void duplicate(JAXBEntity oldEntity, JAXBEntity newEntity);
	}

	private final Map<UUID, JAXBEntity> loaded = new HashMap<>();
	private final Map<Path, JAXBEntity> externalLoaded = new HashMap<>();

	private final List<CreateEntityListener> createListeners = new ArrayList<>();
	private final List<DeleteEntityListener> deleteListeners = new ArrayList<>();
	private final List<DuplicateEntityListener> duplicateListeners = new ArrayList<>();
	private final String extension = ".owl";

	@SuppressWarnings("unchecked")
	public synchronized <T extends JAXBEntity> T loadEntity(String fileName, Class<T> target)
			throws JAXBException, IOException {
		Path path = generatePath(fileName, target);
		if (Files.notExists(path))
			throw new FileNotFoundException(path + " not found");

		UUID id;
		if (isUUID(path) && loaded.containsKey(id = convertPathToUUID(path))) {
			return (T) loaded.get(id);
		}

		T instance = new JAXBHelper<>(path, target).getInstance();
		if (!isUUID(instance.getPath())) {
			String oldName = instance.getFileName();
			String newName = generateFileName();

			instance.getJaxbHelper().renameEntity(newName);
			LoggerMessage massage = new LoggerMessage(LogLevel.WARNING);
			massage.setName("Bad ID instance [" + oldName + "]");
			massage.setMessage("Instance [" + oldName + "] is bad ID. New ID=[" + newName + "]");
			OwlLogger.registerMessage(massage);
		}
		instance.getJaxbHelper().enableAutoSave(e -> {
			OwlLogger.registerException(1, e);
		});
		loaded.put(convertPathToUUID(instance.getPath()), instance);
		return instance;
	}

	public synchronized <T extends JAXBEntity> T createEntity(Class<T> target) throws JAXBException, IOException {
		Path path = generatePath(generateFileName(), target);

		if (Files.exists(path))
			throw new JAXBException("[" + path + "] alredy exist");
		T instance = new JAXBHelper<>(path, target).getInstance();
		instance.getJaxbHelper().enableAutoSave(e -> {
			OwlLogger.registerException(1, e);
		});
		loaded.put(convertPathToUUID(instance.getPath()), instance);
		instance.save();
		notifyCreateChangeListeners(instance);

		return instance;
	}

	@SuppressWarnings("unchecked")
	public synchronized <T extends JAXBEntity> T createOrLoadExternalEntity(Path path, Class<T> target)
			throws JAXBException, IOException {
		if (externalLoaded.containsKey(path)) {
			return (T) externalLoaded.get(path);
		}

		T instance = new JAXBHelper<>(path, target).getInstance();
		instance.getJaxbHelper().enableAutoSave(e -> {
			OwlLogger.registerException(1, e);
		});
		externalLoaded.put(path, instance);
		instance.save();
		notifyCreateChangeListeners(instance);

		return instance;
	}

	public synchronized <T extends JAXBEntity> T createOrLoadModuleConfiguration(OwlookModule module, Class<T> target)
			throws JAXBException, IOException {
		Path path = ProjectPath.MODULE_CONFIG.getPath().resolve(module.getClass().getModule().getName() + ".owl");
		return createOrLoadExternalEntity(path, target);
	}

	public synchronized JAXBEntity createOrLoadModuleConfiguration(OwlookModule module)
			throws JAXBException, IOException, ModuleHasNoConfiguration {
		Path path = ProjectPath.MODULE_CONFIG.getPath().resolve(module.getClass().getModule().getName() + ".owl");
		return createOrLoadExternalEntity(path, module.getConfigTarget());
	}

	public synchronized <T extends JAXBEntity> List<T> loadAllEntities(Class<T> target) throws IOException {

		return Files.find(getConfigPath(target), 1, (p, basicFileAttributes) -> {
			return p.getFileName().toString().endsWith(extension);
		}).map(p -> {
			try {
				return loadEntity(p.getFileName().toString(), target);
			} catch (JAXBException e) {
				OwlLogger.registerException(1, e);
			} catch (FileNotFoundException e) {
				LoggerMessage message = new LoggerMessage(LogLevel.WARNING);
				message.setName("Instance Not Found");
				message.setMessage("Instance [" + p.getFileName() + "] not found in " + target.getPackageName());
				OwlLogger.registerMessage(message);
			} catch (IOException e) {
				OwlLogger.registerException(1, e);
			}
			return null;
		}).collect(Collectors.toList());

	}

	public JAXBEntity tryParseEntity(Path path) throws Nullable, FileNotFoundException, JAXBEntityValidateException {
		if (Files.notExists(path)) {
			throw new FileNotFoundException(path + " not found");
		}

		JAXBEntity instance;
		for (OwlookModule module : ModuleLoader.INSTANCE.loadModules()) {
			try {
				for (Class<? extends JAXBEntity> targetClass : module.getJaxbEntities()) {
					instance = new JAXBHelper<>(path, targetClass).getInstance();
					instance.validate();
					instance.initialize();
					return instance;
				}
			} catch (ModuleHasNoProvideEntities e) {
			} catch (JAXBEntityValidateException e) {
				throw e;
			} catch (IOException | JAXBException e) {
			}
		}

		throw new Nullable();
	}

	public synchronized <T extends JAXBEntity> T duplicateEntity(T entity) throws JAXBException, IOException {
		@SuppressWarnings("unchecked")
		T newEntity = (T) createEntity(entity.getClass());
		newEntity.syncWith(entity);
		notifyDuplicateChangeListeners(entity, newEntity);
		return newEntity;
	}

	// TODO: Добавить логирование
	public synchronized <T extends JAXBEntity> boolean deleteEntity(T entity) {
		try {
			Files.delete(entity.getPath());
			
			if (isUUID(entity.getPath())) {
				loaded.remove(convertPathToUUID(entity.getPath()));
			}
			externalLoaded.remove(entity.getPath());

			if (entityResourcesExist(entity)) {
				FileUtils.deleteDirectory(getEntityResourcesPath(entity).toFile());
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

				@Override
				public JAXBEntity getEntity() {
					return entity;
				}
			});
			notifyDeleteChangeListeners(entity);
			return true;
		} catch (IOException e) {
			OwlLogger.registerException(1, e);
		}
		return false;
	}

	public void addCreateChangeListener(CreateEntityListener listener) {
		createListeners.add(listener);
	}

	public void removeCreateChangeListener(CreateEntityListener listener) {
		createListeners.remove(listener);
	}

	public void addDeleteChangeListener(DeleteEntityListener listener) {
		deleteListeners.add(listener);
	}

	public void removeDeleteChangeListener(DeleteEntityListener listener) {
		deleteListeners.remove(listener);
	}

	public void addDuplicateChangeListener(DuplicateEntityListener listener) {
		duplicateListeners.add(listener);
	}

	public void removeDuplicateChangeListener(DuplicateEntityListener listener) {
		duplicateListeners.remove(listener);
	}

	private void notifyCreateChangeListeners(JAXBEntity entity) {
		createListeners.forEach(i -> i.create(entity));
	}

	private void notifyDeleteChangeListeners(JAXBEntity entity) {
		deleteListeners.forEach(i -> i.delete(entity));
	}

	private void notifyDuplicateChangeListeners(JAXBEntity oldEntity, JAXBEntity newEntity) {
		duplicateListeners.forEach(i -> i.duplicate(oldEntity, newEntity));
	}

	private Path getConfigPath(Class<? extends JAXBEntity> target) {
		Path path = ProjectPath.CONFiG.getPath().resolve(target.getPackageName()).toAbsolutePath();

		if (Files.notExists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				OwlLogger.registerException(1, e);
			}
		}

		return path;
	}

	private Path generatePath(String fileName, Class<? extends JAXBEntity> target) {
		Path path = getConfigPath(target);
		if (!fileName.endsWith(extension)) {
			return path.resolve(fileName + extension);
		} else {
			return path.resolve(fileName);
		}
	}

	public static Path getEntityResourcesPath(JAXBEntity entity) {
		Path entityResPath = ProjectPath.RESOURCES.getPath().resolve(entity.getFileName()).toAbsolutePath();
		if (Files.notExists(entityResPath)) {
			try {
				Files.createDirectory(entityResPath);
			} catch (IOException e) {
				OwlLogger.registerException(1, e);
			}
		}

		return entityResPath;
	}

	public static boolean entityResourcesExist(JAXBEntity entity) {
		Path resPath = ProjectPath.RESOURCES.getPath().toAbsolutePath();
		Path entityResPath = resPath.resolve(entity.getFileName()).toAbsolutePath();

		if (Files.exists(resPath)) {
			if (Files.exists(entityResPath)) {
				try (Stream<Path> files = Files.list(entityResPath)) {
					if (files.count() > 0) {
						return true;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	private String generateFileName() {
		return UUID.randomUUID().toString() + extension;
	}

	public static String convertPathToString(Path path) {
		String fileName = path.getFileName().toString();
		int ind = fileName.lastIndexOf(".");
		return fileName.substring(0, ind);
	}

	public static UUID convertPathToUUID(Path path) {
		return UUID.fromString(convertPathToString(path));
	}

	public static boolean isUUID(Path path) {
		try {
			UUID.fromString(convertPathToString(path));
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

}
