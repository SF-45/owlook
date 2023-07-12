package space.sadfox.owlook.jaxb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.utils.OwlLogger;
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
	
	public final boolean isExternalEntity() {
		return externalEntity;
	}
	
	void setExternalEnity(boolean externalEntity) {
		this.externalEntity = externalEntity;
	}

	public final Path getPath() {
		return getJaxbHelper().getPath();
	}

	public final ChangeHistory getChangeHistory() {
		if (changeHistory == null)
			changeHistory = new ChangeHistory(this);
		return changeHistory;
	}

	public final void save() {
		getJaxbHelper().save();
	}

	public final void saveImmediately() {
		getJaxbHelper().saveImmediately();
	}

	public final String getFileName() {
		String fileName = getPath().getFileName().toString();
		int ind = fileName.lastIndexOf(".");
		return fileName.substring(0, ind);
	}

	public final UUID getId() {
		return UUID.fromString(getFileName());

	}

	public abstract String getTitle();

	public abstract void setTitle(String title);

	public abstract Controller getConfigController() throws IOException, ControllerNotDefined;

	public abstract void validate() throws JAXBEntityValidateException;

	public abstract void initialize();

	public abstract void syncWith(JAXBEntity entity);

	public final void addEntityChangeListener(EntityChangeListener listener) {
		changeListeners.add(listener);
	}

	public final void removeEntityChangeListener(EntityChangeListener listener) {
		changeListeners.remove(listener);
	}

	protected final void notifyEntityChangeListeners(EntityChangeListener.Change change) {
		changeListeners.forEach(listener -> listener.change(change));
	}

	static Path getConfigPath(Class<? extends JAXBEntity> target) {
		Path path = ProjectPath.CONFiG.getPath().resolve(target.getPackageName()).toAbsolutePath();

		if (Files.isDirectory(path) && Files.notExists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				OwlLogger.registerException(1, e);
			}
		}

		return path;
	}

	public final Path getResourcesPath() {
		Path entityResPath = ProjectPath.RESOURCES.getPath().resolve(getFileName())
				.toAbsolutePath();
		if (Files.notExists(entityResPath)) {
			try {
				Files.createDirectory(entityResPath);
			} catch (IOException e) {
				OwlLogger.registerException(1, e);
			}
		}

		return entityResPath;
	}

	public final boolean resourcesExist() {
		Path resPath = ProjectPath.RESOURCES.getPath().toAbsolutePath();
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
