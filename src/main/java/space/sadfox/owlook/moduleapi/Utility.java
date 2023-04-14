package space.sadfox.owlook.moduleapi;

public interface Utility {
	default String getIdentifier() {
		return this.getClass().getName();
	}
	public String getName();
	public String getDescription();

}
