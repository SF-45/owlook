package space.sadfox.owlook.components.bootpatch;

import java.util.Arrays;

import space.sadfox.owlook.moduleapi.ModuleBac;

public class BootPathDao {
	
	private BootPath bootPath;

	public BootPathDao(BootPath bootPath) {
		this.bootPath = bootPath;
	}
	
	
	public ModuleBootList getModuleBootList(ModuleBac moduleBac) {
		for (ModuleBootList moduleBootList : bootPath.getWorkspaces()) {
			if (moduleBootList.getName().equals(moduleBac.getName()) && moduleBootList.getVersion().equals(moduleBac.getVersion())) {
				return moduleBootList;
			}
		}
		ModuleBootList workspacePathes = new ModuleBootList();
		workspacePathes.setName(moduleBac.getName());
		workspacePathes.setVersion(moduleBac.getVersion());
		bootPath.getWorkspaces().add(workspacePathes);
		return workspacePathes;
	}
	
	public void addPathToModule(ModuleBac moduleBac, String ... pathes) {
		ModuleBootList workspacePathes = getModuleBootList(moduleBac);
		workspacePathes.getPaths().addAll(Arrays.asList(pathes));
	}
	
	public void removePathToModule(ModuleBac moduleBac, String ... pathes) {
		ModuleBootList workspacePathes = getModuleBootList(moduleBac);
		workspacePathes.getPaths().removeAll(Arrays.asList(pathes));
	}
}
