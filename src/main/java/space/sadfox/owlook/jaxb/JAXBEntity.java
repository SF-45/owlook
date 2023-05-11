package space.sadfox.owlook.jaxb;

import java.nio.file.Path;
import java.util.List;

import space.sadfox.owlook.moduleapi.ChangeHistoryKeeping;


public abstract class JAXBEntity implements ChangeHistoryKeeping {
	
	private JAXBHelper<?> jaxbHelper;
	private ChangeHistory changeHistory;
	private List<EntityChangeListener> changeListeners;
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
	public abstract String getExtension();
	//public abstract PreLoadAction getPreLoadAction();
	public abstract void initialize();
	public abstract boolean validate();
	
	public void addEntityChangeListener(EntityChangeListener listener) {
		changeListeners.add(listener);
	}

	public void removeEntityChangeListener(EntityChangeListener listener) {
		changeListeners.remove(listener);
	}

	void notifyEntityChangeListeners(EntityChangeListener.Change change) {
		changeListeners.forEach(l -> l.change(change));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
