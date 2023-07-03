package space.sadfox.owlook.jaxb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.moduleapi.ChangeHistoryKeeping;
import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.utils.ErrorLogger;
import space.sadfox.owlook.utils.ProjectPath;

public abstract class JAXBEntity implements ChangeHistoryKeeping {

	private JAXBHelper<?> jaxbHelper;
	private ChangeHistory changeHistory;
	private List<EntityChangeListener> changeListeners = new ArrayList<>();
	private boolean externalEntity;

	public JAXBHelper<?> getJaxbHelper() {
		return jaxbHelper;
	}

	void setJaxbHelper(JAXBHelper<?> jaxbHelper) {
		this.jaxbHelper = jaxbHelper;
	}
	
	public boolean isExternalEntity() {
		return externalEntity;
	}
	
	void setExternalEnity(boolean externalEntity) {
		this.externalEntity = externalEntity;
	}

	public Path getPath() {
		return getJaxbHelper().getPath();
	}

	public ChangeHistory getChangeHistory() {
		if (changeHistory == null)
			changeHistory = new ChangeHistory(this);
		return changeHistory;
	}

	public void save() {
		getJaxbHelper().save();
	}

	public void saveImmediately() {
		getJaxbHelper().saveImmediately();
	}

	public String getFileName() {
		String fileName = getPath().getFileName().toString();
		int ind = fileName.lastIndexOf(".");
		return fileName.substring(0, ind);
	}

	public UUID getId() {
		return UUID.fromString(getFileName());

	}

	public abstract String getTitle();

	public abstract void setTitle(String title);

	public abstract Controller getConfigController() throws IOException, ControllerNotDefined;

	public abstract String getExtension();

	public abstract void validate() throws JAXBEntityValidateException;

	public abstract void initialize();

	public abstract void syncWith(JAXBEntity entity);

	public void addEntityChangeListener(EntityChangeListener listener) {
		changeListeners.add(listener);
	}

	public void removeEntityChangeListener(EntityChangeListener listener) {
		changeListeners.remove(listener);
	}

	protected void notifyEntityChangeListeners(EntityChangeListener.Change change) {
		changeListeners.forEach(listener -> listener.change(change));
	}

	static Path getConfigPath(Class<? extends JAXBEntity> target) {
		Path path = ProjectPath.CONFiG.getPath().resolve(target.getPackageName()).toAbsolutePath();

		if (Files.isDirectory(path) && Files.notExists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				ErrorLogger.registerException(e);
			}
		}

		return path;
	}

	public Path getResourcesPath() {
		Path entityResPath = ProjectPath.RESOURCES.getPath().resolve(getClass().getPackageName()).resolve(getFileName())
				.toAbsolutePath();
		if (Files.notExists(entityResPath)) {
			try {
				Files.createDirectory(entityResPath);
			} catch (IOException e) {
				ErrorLogger.registerException(e);
			}
		}

		return entityResPath;
	}

	public boolean resourcesExist() {
		Path resPath = ProjectPath.RESOURCES.getPath().resolve(getClass().getPackageName()).toAbsolutePath();
		Path entityResPath = resPath.resolve(getFileName()).toAbsolutePath();

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

}
