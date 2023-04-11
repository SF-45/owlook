package space.sadfox.owlook.moduleapi;

import space.sadfox.owlook.jaxb.JAXBEntity;

public abstract class Module {
	public abstract String getName();
	public abstract String getDescription();
	public abstract String getVersion();
	public abstract Class<? extends JAXBEntity> getEntityClass();

}
