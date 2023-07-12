package space.sadfox.owlook.moduleapi;

import java.util.List;

import space.sadfox.owlook.jaxb.JAXBEntity;
import space.sadfox.owlook.utils.Nullable;

public interface OwlookModule {
	String getShortModuleDescription();
	String getModuleDescription();
	String getModuleVersion();
	void initModule();
	List<Class<? extends JAXBEntity>> getJaxbEntities() throws Nullable;
	Class<? extends JAXBEntity> getConfigTarget() throws ModuleHasNoConfiguration;
}
