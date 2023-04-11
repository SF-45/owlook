package space.sadfox.owlook.components.bootpatch;

import java.util.Arrays;

import space.sadfox.owlook.moduleapi.Module;

public class BootPathDao {
	
	private BootPath bootPath;

	public BootPathDao(BootPath bootPath) {
		this.bootPath = bootPath;
	}
	
	
	public ModuleBootList getModuleBootList(Module module) {
		for (ModuleBootList moduleBootList : bootPath.getWorkspaces()) {
			if (moduleBootList.getName().equals(module.getName()) && moduleBootList.getVersion().equals(module.getVersion())) {
				return moduleBootList;
			}
		}
		ModuleBootList workspacePathes = new ModuleBootList();
		workspacePathes.setName(module.getName());
		workspacePathes.setVersion(module.getVersion());
		bootPath.getWorkspaces().add(workspacePathes);
		return workspacePathes;
	}
	
	public void addPathToModule(Module module, String ... pathes) {
		ModuleBootList workspacePathes = getModuleBootList(module);
		workspacePathes.getPaths().addAll(Arrays.asList(pathes));
	}
	
	public void removePathToModule(Module module, String ... pathes) {
		ModuleBootList workspacePathes = getModuleBootList(module);
		workspacePathes.getPaths().removeAll(Arrays.asList(pathes));
	}
}
