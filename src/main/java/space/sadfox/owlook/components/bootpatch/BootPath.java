package space.sadfox.owlook.components.bootpatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import space.sadfox.owlook.jaxb.JAXBEntity;
import space.sadfox.owlook.ui.base.Controller;
import space.sadfox.owlook.utils.Nullable;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class BootPath extends JAXBEntity {

	private ObservableList<ModuleBootList> workspaces = FXCollections.observableArrayList();

	@XmlElement(name = "Workspace")
	public List<ModuleBootList> getWorkspaces() {
		return workspaces;
	}

	public ObservableList<ModuleBootList> workspacesProperty() {
		return workspaces;
	}

	@Override
	public List<Object> getProperties() {
		return Arrays.asList(workspaces);
	}

	@Override
	public String getExtension() {
		return ".bootlist";
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize() {

	}

	@Override
	public void validate() {
		
	}

	@Override
	public Controller getConfigController() throws IOException, Nullable {
		throw new Nullable();
	}

	@Override
	public void syncWith(JAXBEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

}
