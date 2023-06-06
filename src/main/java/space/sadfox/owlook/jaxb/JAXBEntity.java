package space.sadfox.owlook.jaxb;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import space.sadfox.owlook.moduleapi.ChangeHistoryKeeping;
import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.utils.Nullable;


public abstract class JAXBEntity implements ChangeHistoryKeeping {
	
	private JAXBHelper<?> jaxbHelper;
	private ChangeHistory changeHistory;
	private List<EntityChangeListener> changeListeners = new ArrayList<>();
	private Path path;

	public JAXBHelper<?> getJaxbHelper() {
		return jaxbHelper;
	}

	void setJaxbHelper(JAXBHelper<?> jaxbHelper) {
		this.jaxbHelper = jaxbHelper;
	}

	public Path getPath() {
		return path;
	}

	void setPath(Path path) {
		this.path = path;
	}
	
	public ChangeHistory getChangeHistory() {
		if (changeHistory == null) changeHistory = new ChangeHistory(this);
		return changeHistory;
	}
	
	public void save() {
		getJaxbHelper().save();
	}
	public void saveImmediately() {
		getJaxbHelper().saveImmediately();
	}
	
	public String getFileName() {
		String fileName = path.getFileName().toString();
		int ind = fileName.lastIndexOf(".");
		return fileName.substring(0, ind);
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
