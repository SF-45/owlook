package space.sadfox.owlook.components.bootpatch;

import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import space.sadfox.owlook.moduleapi.ChangeHistoryKeeping;

@XmlAccessorType(XmlAccessType.NONE)
public class ModuleBootList implements ChangeHistoryKeeping {
	
	private StringProperty name = new SimpleStringProperty();
	private StringProperty version = new SimpleStringProperty();
	private ObservableList<String> paths = FXCollections.observableArrayList();
	
	
	@XmlAttribute(name = "Name")
	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}
	
	public StringProperty nameProperty() {
		return name;
	}

	@XmlAttribute(name = "Version")
	public String getVersion() {
		return version.get();
	}

	public void setVersion(String version) {
		this.version.set(version);
	}
	
	public StringProperty versionProperty() {
		return version;
	}

	@XmlElementWrapper(name = "Paths")
	@XmlElement(name = "path")
	public List<String> getPaths() {
		return paths;
	}
	
	public ObservableList<String> pathsProperty() {
		return paths;
	}

	@Override
	public List<Object> getProperties() {
		return Arrays.asList(name, version, paths);
	}

	


}
