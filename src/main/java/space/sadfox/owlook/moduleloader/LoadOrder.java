package space.sadfox.owlook.moduleloader;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Optional;

import space.sadfox.owlook.base.moduleapi.OwlookModulePack;

class LoadOrder extends ArrayDeque<OwlookModulePack> {

	private static final long serialVersionUID = 5744159922572199050L;
	
	private final Collection<OwlookModulePack> pool;

	public LoadOrder(Collection<OwlookModulePack> c) {
		pool = c;
		
		pool.forEach(this::build);
		
	}
	
	private void build(OwlookModulePack owlookModulePack) {
		if (matchOwlookModule(owlookModulePack)) return;
		for (String moduleName : owlookModulePack.MODILE_INFO.requiresOwlookModules()) {
			Optional<OwlookModulePack> optional = findModulePack(moduleName);
			if (optional.isPresent()) {
				build(optional.get());
			}
		}
		add(owlookModulePack);
	}
	
	private Optional<OwlookModulePack> findModulePack(String moduleName) {
		return pool.stream().filter(pack -> pack.MODILE_INFO.moduleName().equals(moduleName)).findFirst();
	}
	
	private boolean matchOwlookModule(OwlookModulePack owlookModulePack) {
		return stream().anyMatch(pack -> pack.MODILE_INFO.moduleName().equals(owlookModulePack.MODILE_INFO.moduleName()));
	}
	
	

}
