package space.sadfox.owlook.configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import space.sadfox.owlook.jaxb.ControllerNotDefined;
import space.sadfox.owlook.jaxb.JAXBEntity;
import space.sadfox.owlook.jaxb.JAXBEntityValidateException;
import space.sadfox.owlook.ui.base.Controller;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "owlookConfiguration")
public class OwlookConfigurationEntity extends JAXBEntity {
	
	private final StringProperty title = new SimpleStringProperty("title");
	private final IntegerProperty loggingDepth = new SimpleIntegerProperty(1);
	private final BooleanProperty debugMode = new SimpleBooleanProperty(true);

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
	public boolean getDebugMode() {
		return debugModeProperty().get();
	}
	
	public void setDebugMode(boolean debugMode) {
		debugModeProperty().set(debugMode);
	}
	
	public BooleanProperty debugModeProperty() {
		return debugMode;
	}

	@Override
	public Controller getConfigController() throws IOException, ControllerNotDefined {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Object> getProperties() {
		return Arrays.asList(title, loggingDepth, debugMode);
	}

	@Override
	public void validate() throws JAXBEntityValidateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void syncWith(JAXBEntity entity) {
		if (!getClass().equals(entity.getClass())) return;
		
		OwlookConfigurationEntity newConfig = (OwlookConfigurationEntity) entity;
		
		setLoggingDepth(newConfig.getLoggingDepth());
		setDebugMode(newConfig.getDebugMode());
	}

}
