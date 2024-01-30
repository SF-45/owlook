package space.sadfox.owlook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import space.sadfox.owlook.base.jaxb.ObservedJAXBEntity;
import space.sadfox.owlook.base.moduleapi.VersionFormat;
import space.sadfox.owlook.utils.ConfigurationManager;
import space.sadfox.owlook.utils.Logger;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "owlookConfiguration")
public class OwlookConfiguration extends ObservedJAXBEntity {
	private VersionFormat version;
	private final IntegerProperty loggingDepth = new SimpleIntegerProperty(1);
	private final BooleanProperty debugMode = new SimpleBooleanProperty(true);
	private final BooleanProperty skipModuleManage = new SimpleBooleanProperty(false);
	private final ObservableList<String> modules = FXCollections.observableArrayList();
	
	public VersionFormat getVersion() {
		return version;
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
		return Arrays.asList(loggingDepth, debugMode, modules, skipModuleManage);
	}

	public static OwlookConfiguration instance() {
		Path confPath = Path.of("owlook.conf");
		try {
			return new ConfigurationManager<>(OwlookConfiguration.class).getConfig(confPath);
		} catch (JAXBException | IOException | ClassCastException | ReflectiveOperationException e) {
			Logger.registerException(0, e);
			return null;
		}
	}

	@Override
	protected void initialization() {
		super.initialization();
		try {
			Properties properties = new Properties();
			properties.load(ResourceTarget.class.getResourceAsStream("pom.properties"));
			version = VersionFormat.of(properties.getProperty("version", "0.0.0-default"));
		} catch (IOException e) {
			Logger.registerException(0, e);
		}
	}

}
