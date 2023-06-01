package space.sadfox.owlook.jaxb;

@FunctionalInterface
public interface EntityChangeListener<T extends space.sadfox.owlook.jaxb.EntityChangeListener.Change> {
	public abstract static class Change {
		public abstract boolean wasModify();
		public abstract boolean wasRemoved();
	}

	void change(T change);
}
