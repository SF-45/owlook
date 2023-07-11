package space.sadfox.owlook;

import java.io.IOException;
import java.util.List;

import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.configuration.OwlookConfigurationEntity;
import space.sadfox.owlook.jaxb.EntityLoader;
import space.sadfox.owlook.jaxb.JAXBEntity;
import space.sadfox.owlook.moduleapi.Module;
import space.sadfox.owlook.moduleapi.ModuleHasNoConfiguration;
import space.sadfox.owlook.utils.Nullable;

public class OwlookModuleProvider implements Module {

	@Override
	public String getModuleName() {
		return "owlook";
	}

	@Override
	public String getModuleDescription() {
		return "General Module";
	}

	@Override
	public String getModuleVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initModule() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<Class<? extends JAXBEntity>> getJaxbEntities() throws Nullable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends JAXBEntity> getConfigTarget() throws ModuleHasNoConfiguration {
		return OwlookConfigurationEntity.class;
	}
	
	public synchronized static OwlookConfigurationEntity getConfig() {
		try {
			return EntityLoader.INSTANCE.createOrLoadModuleConfiguration(new OwlookModuleProvider(), OwlookConfigurationEntity.class);
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
