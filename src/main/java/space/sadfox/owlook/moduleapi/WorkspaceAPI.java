package space.sadfox.owlook.moduleapi;


import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.jaxb.JAXBEntity;



public interface WorkspaceAPI {
	WorkspaceUI getWorkspaceUI(JAXBEntity entity) throws JAXBException;
}
