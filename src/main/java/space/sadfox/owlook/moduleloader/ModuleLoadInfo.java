package space.sadfox.owlook.moduleloader;

import space.sadfox.owlook.base.moduleapi.OwlookModule;
import space.sadfox.owlook.base.moduleapi.OwlookModulePack;

public class ModuleLoadInfo {
	public enum Status {
		OK, 
		ERROR;
	}
	
	private OwlookModule owlookModule;
	public final OwlookModulePack PACK;

	public final Status STATUS;
	public final String MASSAGE;
	public ModuleLoadInfo(OwlookModulePack owlookModulePack, Status status, String massage) {
		PACK = owlookModulePack;
		STATUS = status;
		MASSAGE = massage;
	}
	
	

}
