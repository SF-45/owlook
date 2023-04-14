package space.sadfox.owlook.moduleapi;

import space.sadfox.owlook.jaxb.JAXBEntity;

public interface ModuleBac {
	public abstract String getName();
	public abstract String getDescription();
	public abstract String getVersion();
	public abstract Class<? extends JAXBEntity> getEntityClass();
	default Service getServices() {
		return null;
	}
	default Utility getUtilities() {
		return null;
	}
	default Workspace getWorkspaces() {
		return null;
	}

}
