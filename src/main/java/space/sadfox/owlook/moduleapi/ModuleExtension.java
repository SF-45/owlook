package space.sadfox.owlook.moduleapi;

public interface ModuleExtension {
	default String getIdentifier() {
		return this.getClass().getName();
	}
	String getModuleExtensionName();
	String getModuleExtensionDescription();

}
