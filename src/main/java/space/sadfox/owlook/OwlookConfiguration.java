package space.sadfox.owlook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import space.sadfox.owlook.base.jaxb.JAXBEntity;
import space.sadfox.owlook.utils.EntityLoader;
import space.sadfox.owlook.utils.OwlLogger;
import space.sadfox.owlook.utils.ProjectPath;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "owlookConfiguration")
public class OwlookConfiguration extends JAXBEntity {
	
	private final StringProperty title = new SimpleStringProperty("title");
	private final IntegerProperty loggingDepth = new SimpleIntegerProperty(1);
	private final BooleanProperty debugMode = new SimpleBooleanProperty(true);
	private final BooleanProperty skipModuleManage = new SimpleBooleanProperty(false);
	private final ObservableList<String> modules = FXCollections.observableArrayList();

	@Override
	@XmlElement
	public String getTitle() {
		return titleProperty().get();
	}

	@Override
	public void setTitle(String title) {
		titleProperty().set(title);
		
	}
	
	public StringProperty titleProperty() {
		return title;
	}
	
	@XmlElement
	public int getLoggingDepth() {
		return loggingDepthProperty().get();
	}
	
	public void setLoggingDepth(int loggingDepth) {
		loggingDepthProperty().set(loggingDepth);
	}
	
	public IntegerProperty loggingDepthProperty() {
		return loggingDepth;
	}

	@XmlElement
	public boolean isDebugMode() {
		return debugModeProperty().get();
	}
	
	public void setDebugMode(boolean debugMode) {
		debugModeProperty().set(debugMode);
	}
	
	public BooleanProperty debugModeProperty() {
		return debugMode;
	}
	
	@XmlElement
	public boolean isSkipModuleManage() {
		return skipModuleManageProperty().get();
	}
	
	public void setSkipModuleManage(boolean skipModuleManage) {
		skipModuleManageProperty().set(skipModuleManage);
	}
	
	public BooleanProperty skipModuleManageProperty() {
		return skipModuleManage;
	}
	
	@XmlElementWrapper(name = "modules")
	@XmlElement(name = "module")
	public List<String> getModules() {
		return modulesProperty();
	}
	
	public ObservableList<String> modulesProperty() {
		return modules;
	}
	
	@Override
	public List<Object> getProperties() {
		return Arrays.asList(title, loggingDepth, debugMode, modules, skipModuleManage);
	}

	@Override
	public void validate() {
		
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void syncWith(JAXBEntity entity) {
		if (!getClass().equals(entity.getClass())) return;
		
		OwlookConfiguration newConfig = (OwlookConfiguration) entity;
		
		setLoggingDepth(newConfig.getLoggingDepth());
		setDebugMode(newConfig.isDebugMode());
	}
	
	public static OwlookConfiguration instance() {
		Path confPath = ProjectPath.MODULE_CONFIG.getPath().resolve("Owlook.owl");
		try {
			return EntityLoader.INSTANCE.createOrLoadExternalEntity(confPath, OwlookConfiguration.class);
		} catch (JAXBException | IOException e) {
			OwlLogger.registerException(0, e);
			return null;
		}
	}

}
