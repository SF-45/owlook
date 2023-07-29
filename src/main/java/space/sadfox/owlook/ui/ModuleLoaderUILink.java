package space.sadfox.owlook.ui;

public class ModuleLoaderUILink {
	
	private volatile static ModuleLayer moduleLayer;
	
	static synchronized void setModuleLayer(ModuleLayer moduleLayer) {
		ModuleLoaderUILink.moduleLayer = moduleLayer;
	}
	
	public synchronized static ModuleLayer getModuleLayer() {
		return moduleLayer;
	}
}
